package com.soundcu.ofac.services;

import com.soundcu.ofac.model.Entry;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntryService {
    private static final String ADDRESS = "address";
    private static final String BIRTH_DATE = "birthdate";
    private static final String CITY = "city";
    private static final String COUNTRY = "country";
    private static final String ID = "id_number";
    private static final String NAME = "name";
    private static final String POSTAL_CODE = "postal_code";
    private static final String TAX_ID = "ssn";
    private static final String STATE = "state";

    @Autowired
    private EntryBuilder entryBuilder;

    /**
     * Filters our search results based on type (entity, individual, or vessel).
     *
     * @param minimumScore score
     * @param entity       entity
     * @param type         type
     * @return List
     */
    public List<Entry> filterByType(int minimumScore, JSONObject entity, String type) {
        return search(minimumScore, entity).stream().filter((entry) ->
        {
            if (entry.getType() == null)
                return false;

            return entry.getType().equalsIgnoreCase(type);
        }).collect(Collectors.toList());
    }

    /**
     * Transforms a String to a Date.
     *
     * @param date
     * @return Date
     */
    private Date parseDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(date);
        } catch (ParseException e) {
            return new Date();
        }
    }

    /**
     * Checks whether or not an entity is active on the list.
     *
     * @param entry
     * @return boolean
     */
    private boolean isExcluded(Entry entry) {
        if (entry.getEnd_date() == null && entry.getStart_date() == null)
            return false;

        Date today = new Date();
        if (entry.getEnd_date() != null && today.after(parseDate(entry.getEnd_date())))
            return true;

        if (entry.getStart_date() != null && today.before(parseDate(entry.getStart_date())))
            return true;

        return false;
    }

    /**
     * In addition to fuzzy matching on the name, check to see if other fields are equal.
     *
     * @param entry
     * @param entity
     * @return boolean
     */
    private boolean isMatch(Entry entry, JSONObject entity) {
        boolean isMatch = false;
        if (isMatchOnAddress(entry, entity))
            isMatch = true;

        if (isMatchOnBirthday(entry, entity))
            isMatch = true;

        if (isMatchOnId(entry, entity))
            isMatch = true;

        return isMatch;
    }

    /**
     * Returns if request matches an entry's address.
     *
     * @param entry
     * @param entity
     * @return boolean
     */
    private boolean isMatchOnAddress(Entry entry, JSONObject entity) {
        if (!entity.has(ADDRESS) || !entity.has(CITY) || !entity.has(COUNTRY))
            return false;
        else if (!entity.has(POSTAL_CODE) || !entity.has(STATE))
            return false;
        else if (entry.getAddresses() == null)
            return false;

        return entry.getAddresses().stream().anyMatch((addr) ->
        {
            if (!addr.getAddress().equalsIgnoreCase(entity.getString(ADDRESS)))
                return false;

            if (!addr.getCity().equalsIgnoreCase(entity.getString(CITY)))
                return false;

            if (!addr.getCountry().equalsIgnoreCase(entity.getString(COUNTRY)))
                return false;

            if (!addr.getPostal_code().equalsIgnoreCase(entity.getString(POSTAL_CODE)))
                return false;

            if (!addr.getState().equalsIgnoreCase(entity.getString(STATE)))
                return false;

            return true;
        });
    }

    /**
     * Returns if request matches an entry's birth date.
     *
     * @param entry
     * @param entity
     * @return boolean
     */
    private boolean isMatchOnBirthday(Entry entry, JSONObject entity) {
        if (!entity.has(BIRTH_DATE) || entry.getDates_of_birth() == null)
            return false;

        String entityDob = entity.getString(BIRTH_DATE);
        return entry.getDates_of_birth().stream().anyMatch((dob) -> {
            return dob.equals(entityDob);
        });
    }

    /**
     * Returns if request matches an entry's ID number.
     *
     * @param entry
     * @param entity
     * @return boolean
     */
    private boolean isMatchOnId(Entry entry, JSONObject entity) {
        //if (!(entity.has(ID) && entity.has(TAX_ID)) || entry.getIds() == null) //Does it have to be id AND tax id???
        if (!(entity.has(ID) || entity.has(TAX_ID)) || entry.getIds() == null)
            return false;

        String entityId = entity.has(ID) ? entity.getString(ID).replaceAll("[^a-zA-Z0-9]", "") : null;
        String taxId = entity.has(TAX_ID) ? entity.getString(TAX_ID).replaceAll("[^a-zA-Z0-9]", "") : null;

        return entry.getIds().stream().anyMatch((id) ->
        {
            if (id.getNumber() == null)
                return false;

            String idNumber = id.getNumber().replaceAll("[^a-zA-Z0-9]", "");
            return idNumber.equalsIgnoreCase(entityId) || idNumber.equalsIgnoreCase(taxId);
        });
    }

    /**
     * Searches the list for a fuzzy name match above the given threshold or an exact match
     * on secondary fields (address, birth date, or ID number).
     *
     * @param minimumScore score
     * @param entity       entity
     * @return List list
     */
    public List<Entry> search(int minimumScore, JSONObject entity) {
        List<Entry> entries = entryBuilder.getEntries();
        return entries.stream().filter((entry) ->
        {
            if (isExcluded(entry))
                return false;

            boolean scoreMatch = false;

            if (entity.has(NAME)) {
                int score = FuzzySearch.tokenSortRatio(entry.getName(), entity.getString(NAME));

                if (score >= minimumScore) {
                    entry.setScore(score);
                    scoreMatch = true;
                } else {
                    List<String> names = Arrays.asList(entry.getName().split(","));
                    int j = 0;
                    while(names != null && !scoreMatch && j < names.size()) {
                        score = FuzzySearch.tokenSortRatio(names.get(j), entity.getString(NAME));
                        if (score >= minimumScore) {
                            entry.setScore(score);
                            scoreMatch = true;
                        }
                        j++;
                    }
                                        
                    if (scoreMatch == false) {
                        List<String> altNames = entry.getAlt_names();
                        int i = 0;
                        while (altNames != null && !scoreMatch && i < altNames.size()) {
                            score = FuzzySearch.tokenSortRatio(altNames.get(i), entity.getString(NAME));
                            if (score >= minimumScore) {
                                entry.setScore(score);
                                scoreMatch = true;
                            }
                            i++;
                        }
                    }
                }
            }
            return scoreMatch || isMatch(entry, entity);
        }).collect(Collectors.toList());
    }
}
