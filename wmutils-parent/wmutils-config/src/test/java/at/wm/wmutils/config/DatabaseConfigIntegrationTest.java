package at.wm.wmutils.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

public class DatabaseConfigIntegrationTest extends AbstractTest {

	@Autowired
	private MessageSource messages;

	@Test
	public void test() {
		Assert.assertEquals("http://xyz.com", messages.getMessage("app.url", null, null));
	}
}