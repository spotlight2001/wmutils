package at.wm.wmutils.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ResourceLoaderAware;

//XXX we expect to be called after std ResourceLoaderAware setter is called
public class DatabaseResourceLoaderRegisterer implements BeanPostProcessor {

	protected final Log logger = LogFactory.getLog(getClass());

	private DatabaseResourceLoader databaseResourceLoader;

	public void setDatabaseResourceLoader(
			DatabaseResourceLoader databaseResourceLoader) {
		this.databaseResourceLoader = databaseResourceLoader;
	}

	public Object postProcessBeforeInitialization(Object bean, String name)
			throws BeansException {
		if (bean instanceof ResourceLoaderAware) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking setResourceLoader on ResourceLoaderAware bean '"
						+ name + "'");
			}
			((ResourceLoaderAware) bean)
					.setResourceLoader(this.databaseResourceLoader);
		}
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String name)
			throws BeansException {
		return bean;
	}
}