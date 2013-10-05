package at.wm.wmutils.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanExpressionException;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;

/** #{beanIdOfDatabaseResolver.key} */
public class DatabaseBeanExpressionResolver extends
		StandardBeanExpressionResolver {

	private static final String EL_PREFIX = "#{";

	@Override
	public Object evaluate(String value, BeanExpressionContext evalContext)
			throws BeansException {
		try {
			return super.evaluate(value, evalContext);
		} catch (BeanExpressionException e) {
			// try our code
			if (value == null || !value.startsWith(EL_PREFIX)) {
				return value;
			}
			// assume expression like: #{dbcfg.app.user}
			// interpret first token as bean
			int indexOfFirstToken = value.indexOf(".");
			String beanName = value.substring(EL_PREFIX.length(),
					indexOfFirstToken);
			Object beanObj = evalContext.getObject(beanName);
			if (beanObj == null || !(beanObj instanceof ConfigInDbSource)) {
				return value;
			}

			// get key
			String key = value.substring(indexOfFirstToken + 1,
					value.length() - 1);

			// ask db for value
			ConfigInDbSource db = (ConfigInDbSource) beanObj;
			Object result = db.getValue(key);
			return result;
		}
	}
}