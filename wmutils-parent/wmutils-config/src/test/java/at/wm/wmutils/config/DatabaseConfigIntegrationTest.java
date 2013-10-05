package at.wm.wmutils.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseConfigIntegrationTest extends AbstractTest {

	@SuppressWarnings("unused")
	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	private TestBeanWithValueAnnotations testBeanWithValueAnnotations;

	@Autowired
	private TestBean testBeanWithXmlSpelInjections;

	@Autowired
	private ConfigInDbSource configInDbSource;

	@Autowired
	private ConfigInDbSource cfg;

	@Test
	public void test() throws Exception {
		// check current value by obj
		String expectedUrl = "http://old";
		String expectedUsername = "admin";

		// check before cache evict
		Assert.assertEquals(expectedUsername,
				testBeanWithXmlSpelInjections.getUsername());
		Assert.assertEquals(expectedUrl, testBeanWithXmlSpelInjections.getUrl());
		Assert.assertEquals(expectedUrl, testBeanWithValueAnnotations.url);

		// change data
		String expectedNewUrl = "http://new";
		Assert.assertEquals(1, super.simpleJdbcTemplate
				.update("update app_config set cfg_value = '" + expectedNewUrl
						+ "' where cfg_key = 'app.url'"));

		// sleep longer than caching time
		Thread.sleep(2 * 1000);

		// recheck values
		// SPEL doesnt reevaluate on runtime
		// -> expect old value
		Assert.assertEquals(expectedUrl, testBeanWithValueAnnotations.url);
		Assert.assertEquals(expectedUrl, testBeanWithXmlSpelInjections.getUrl());
		Assert.assertEquals(expectedUsername,
				testBeanWithXmlSpelInjections.getUsername());

		// this reevaluates
		Assert.assertEquals(expectedNewUrl, cfg.getValueString("app.url"));
		Assert.assertEquals(expectedNewUrl, testBeanWithValueAnnotations.url);
	}
}