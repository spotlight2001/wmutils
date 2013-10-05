package at.wm.wmutils.config;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations = {
		"/at/wm/wmutils/config/wmutils-config-sample-context.xml",
		"/at/wm/wmutils/config/wmutils-config-test-context.xml" })
public abstract class AbstractTest extends
		AbstractTransactionalJUnit4SpringContextTests {
}