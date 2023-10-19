package com.soundcu.ofac.services;

import com.soundcu.ofac.model.Entry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"default", "test"})
@Slf4j
public class EntryBuilderTest {

  @Autowired
  private EntryBuilder entryBuilder;

  @Test
  void fetchEntriesTest() {
    entryBuilder.fetchEntriesFromLocal();
    waitForEntriesLoaded(100, 1);
  }


  public void waitForEntriesLoaded(int upToSeconds, int intervalInSeconds) {
    long startSeconds = System.currentTimeMillis() / 1000;
    long now = System.currentTimeMillis() / 1000;
    logger.debug("waitForEntriesLoaded... starting");
    List<Entry> entries = null;
    while (now < startSeconds + upToSeconds) {
      try {
        entries = entryBuilder.getEntries();
      } catch (Throwable th) {
        //hide the exception, try again.
      }
      if (entries != null && entries.size() > 0) {
        logger.debug("entries loaded");
        return;
      }
      try {
        TimeUnit.SECONDS.sleep(intervalInSeconds);
      } catch (InterruptedException e) {
        //hide the exception, try again.
      }
      now = System.currentTimeMillis() / 1000;
    }
    fail("failed to load entries in " + upToSeconds + " seconds.");
  }
}
