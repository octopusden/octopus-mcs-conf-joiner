package org.octopusden.octopus.multichannelserver.config.joiner;

import org.octopusden.octopus.fileutils.FileUtils;
import org.octopusden.octopus.multichannelserver.XMLConfJoiner;
import org.octopusden.octopus.multichannelserver.config.joiner.xmlutils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TestJoin {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestJoin.class);

  public static final String ORIGINAL_CONF = "../../../test/resources/conf";
  public static final String TEMP = "../../../../target/test-data/temp";
  public static final String TEMP_CONF = "../../../../target/test-data/temp/conf";
  public static final String INIT = "init";

  @Test
  public void testJoin() throws Exception {
    FileUtils.deleteDir(new File(TEMP));
    FileUtils.copy(ORIGINAL_CONF, TEMP);
    XMLConfJoiner.main(new String[]{ TEMP });
    XMLConfJoiner.main(new String[]{ "-join", TEMP });

    //comparing created files and those before splitting
    File result = new File(TEMP_CONF + File.separator + INIT);
    File original = new File(ORIGINAL_CONF + File.separator + INIT);
    String[] resultFiles = result.list();
    String[] originalFiles = original.list();
    for (String name : resultFiles) {
      LOGGER.info("comparing result " + name);
      Assert.assertTrue(String.format("files %s doesn't match", name), XMLUtils.compareXMLFiles(new File(result, name), new File(original, name)));
    }
    for (String name : originalFiles) {
      LOGGER.info("comparing original " + name);
      Assert.assertTrue(String.format("files %s doesn't match", name), XMLUtils.compareXMLFiles(new File(result, name), new File(original, name)));
    }
  }


}

