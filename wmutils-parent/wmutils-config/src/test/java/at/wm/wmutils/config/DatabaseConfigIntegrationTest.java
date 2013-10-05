package at.wm.wmutils.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class DatabaseConfigIntegrationTest extends AbstractTest {

	private final Log log = LogFactory.getLog(getClass());

	@Value("#{dbcfg.app.url}")
	private String urlBySpELAnnotation;

	@Autowired
	private TestBean testBeanWithSpelInjections;

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
				testBeanWithSpelInjections.getUsername());
		Assert.assertEquals(expectedUrl, testBeanWithSpelInjections.getUrl());
		Assert.assertEquals(expectedUrl, urlBySpELAnnotation);

		// change data
		String expectedNewUrl = "http://new";
		Assert.assertEquals(
				1,
				super.jdbcTemplate.update("update app_config set cfg_value = '"
						+ expectedNewUrl + "' where cfg_key = 'app.url'"));

		// sleep longer than caching time
		Thread.sleep(2 * 1000);

		// recheck values
		// SPEL doesnt reevaluate on runtime
		// -> expect old value
		Assert.assertEquals(expectedUrl, urlBySpELAnnotation);
		Assert.assertEquals(expectedUrl, testBeanWithSpelInjections.getUrl());
		Assert.assertEquals(expectedUsername,
				testBeanWithSpelInjections.getUsername());

		// this reevaluates
		Assert.assertEquals(expectedNewUrl, cfg.getValueString("app.url"));
	}
}