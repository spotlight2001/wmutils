package at.wm.wmutils.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class DatabaseELBeanFactoryPostProcessor implements
		BeanFactoryPostProcessor {

	private BeanExpressionResolver beanExpressionResolver;

	public void setBeanExpressionResolver(
			BeanExpressionResolver beanExpressionResolver) {
		this.beanExpressionResolver = beanExpressionResolver;
	}

	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		beanFactory.setBeanExpressionResolver(beanExpressionResolver);
	}

}
