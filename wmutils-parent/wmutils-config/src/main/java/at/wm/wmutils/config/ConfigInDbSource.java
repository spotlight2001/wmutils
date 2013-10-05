package at.wm.wmutils.config;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;

import at.wm.util.SpringExpressionLanguageUtils;

public class ConfigInDbSource implements InitializingBean,
		ApplicationContextAware {

	private final Log log = LogFactory.getLog(getClass());

	private String table = "app_config";
	private String columnKey = "cfg_key";
	private String columnValue = "cfg_value";
	private boolean refreshValueAnnotations = true;
	// either inject or if only one exists - we use this
	private DataSource dataSource;
	private Map<String, Object> cache;

	private ApplicationContext applicationContext;

	public void setRefreshValueAnnotations(boolean refreshValueAnnotations) {
		this.refreshValueAnnotations = refreshValueAnnotations;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public void setColumnKey(String columnKey) {
		this.columnKey = columnKey;
	}

	public void setColumnValue(String columnValue) {
		this.columnValue = columnValue;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setCache(Map<String, Object> cache) {
		this.cache = cache;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	private void refreshAllValueAnnotations() {
		// get all spring beans
		Map<String, Object> beans = applicationContext
				.getBeansOfType(Object.class);

		// foreach spring bean
		for (Object valueAnnotatedBean : beans.values()) {
			for (Field field : valueAnnotatedBean.getClass().getFields()) {

				// check if @Value exists
				Value valueAnnotation = field.getAnnotation(Value.class);
				if (valueAnnotation == null) {
					continue;
				}

				// get #{bla}
				String expressionString = valueAnnotation.value();

				// get spring bean name
				String beanName = SpringExpressionLanguageUtils
						.getBeanName(expressionString);
				String key = SpringExpressionLanguageUtils
						.getKey(expressionString);

				// get spring bean
				ConfigInDbSource other = applicationContext.getBean(beanName,
						getClass());

				// check if its another configured source
				if (this != other) {
					continue;
				}

				// get new cfg value from cfg-source
				Object newValue = other.getValue(key);
				try {
					// set value by reflection
					field.set(valueAnnotatedBean, newValue);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
	}

	/** periodically refresh cache */
	private void refreshCache() {
		this.cache = loadValuesFromDatabase();
		if (log.isDebugEnabled()) {
			log.debug("scheduled cache refresh");
		}
		if (refreshValueAnnotations) {
			refreshAllValueAnnotations();
		}
	}

	private Map<String, Object> loadValuesFromDatabase() {
		final Map<String, Object> result = new ConcurrentHashMap<String, Object>();

		// fill data
		final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		final String sql = String.format(
				"select %s key, %s value from %s order by 1", columnKey,
				columnValue, table);
		RowCallbackHandler rowCallbackHandler = new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String key = rs.getString("key");
				String value = rs.getString("value");
				result.put(key, value);
			}
		};
		jdbc.query(sql, rowCallbackHandler);
		return result;
	}

	/** used by SPEL */
	public Object getValue(String key) {
		if (cache == null) {
			refreshCache();
		}
		Object valueObj = this.cache.get(key);
		Assert.notNull(valueObj);
		return valueObj;
	}

	public String getValueString(String key) {
		return (String) getValue(key);
	}

	public Integer getValueInteger(String key) {
		return Integer.valueOf(getValueString(key));
	}

	/** check if this spring bean was configured correctly */
	public void afterPropertiesSet() throws Exception {
		// check if we have datasource
		if (this.dataSource != null) {
			return;
		}

		// no datasource
		// try get it from app context
		Map<String, DataSource> dataSources = applicationContext
				.getBeansOfType(DataSource.class);
		Assert.isTrue(
				dataSources.size() == 1,
				"no db set, plz either wire a ds to this bean or configure a single ds in app context. i found: '"
						+ dataSources.size() + "'.");
		if (dataSources.size() == 1) {
			this.dataSource = dataSources.values().iterator().next();
		}
	}
}