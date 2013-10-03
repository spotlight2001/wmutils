package at.wm.wmutils.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

public class DatabaseConfigIntegrationTest extends AbstractTest {

	@Autowired
	private MessageSource messages;

	@Test
	public void test() throws Exception {
		// check current value by obj
		Assert.assertEquals("http://xyz.com",
				messages.getMessage("app.url", null, null));

		// change value in db
		Assert.assertEquals(
				1,
				super.simpleJdbcTemplate
						.update("update app_config set cfg_value = 'http://abc.com' where cfg_key = 'app.url'"));

		// sleep longer than caching time
		Thread.sleep(2000);

		// recheck values
		Assert.assertEquals("http://abc.com",
				messages.getMessage("app.url", null, null));
	}
}