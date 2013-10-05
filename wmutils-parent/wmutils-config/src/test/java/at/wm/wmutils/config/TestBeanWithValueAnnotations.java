package at.wm.wmutils.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TestBeanWithValueAnnotations {
	@Value("#{dbcfg.app.url}")
	public String url;
	@Value("#{dbcfg.app.user}")
	public String username;
}