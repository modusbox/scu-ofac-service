package com.soundcu.ofac.services;

import com.soundcu.ofac.model.Entry;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
public class EntryServiceTest {

  @Autowired
  private EntryBuilder entryBuilder;

  @Autowired
  private EntryService entryService;

  private final Integer minimumScore = 90;


  @Test
  public void testSearchWithNameExactly() throws JSONException, IOException {
    String entity = "name=Luciano BRUNETTI";
    String entityString = this.parseForm(entity);
    logger.info(entityString);
    JSONObject entityObj = new JSONObject(entityString);
    List<Entry> entries = entryService.search(minimumScore, entityObj);
    logger.info("entries found " + entries.size());
    assertEquals(1, entries.size());
  }

  @Test
  public void testSearchWithAltNames() throws JSONException, IOException {
    String entity = "name=KARAMAY URBAN CREDIT COOPERATIVES";
    String entityString = this.parseForm(entity);
    logger.info(entityString);
    JSONObject entityObj = new JSONObject(entityString);
    List<Entry> entries = entryService.search(minimumScore, entityObj);
    logger.info("entries found " + entries.size());
    assertEquals(1, entries.size());
  }

  private String parseForm(String body) throws JSONException {
    JSONObject entity = new JSONObject();
    String[] pairs = body.split("&");
    for (String pair : pairs) {
      String[] tokens = pair.split("=");
      String value = tokens.length > 0 ? URLDecoder.decode(tokens[1], StandardCharsets.UTF_8) : null;
      entity.put(tokens[0], value);
    }

    return entity.toString();
  }


}