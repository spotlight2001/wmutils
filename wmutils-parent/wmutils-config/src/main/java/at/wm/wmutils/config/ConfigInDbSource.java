package at.wm.wmutils.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;

public class ConfigInDbSource implements InitializingBean,
		ApplicationContextAware {
	private String table = "app_config";
	private String columnKey = "cfg_key";
	private String columnValue = "cfg_value";
	private Integer cacheSeconds = 60;
	// either inject or if only one exists - we use this
	private DataSource dataSource;
	private Map<String, Object> cache;
	private Date timestampOfLastDbAccess = new Date(Long.MIN_VALUE);

	private ApplicationContext applicationContext;

	public void setTable(String table) {
		this.table = table;
	}

	public void setColumnKey(String columnKey) {
		this.columnKey = columnKey;
	}

	public void setColumnValue(String columnValue) {
		this.columnValue = columnValue;
	}

	public void setCacheSeconds(Integer cacheSeconds) {
		this.cacheSeconds = cacheSeconds;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private Map<String, Object> loadValues() {
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
		timestampOfLastDbAccess = new Date();

		return result;
	}

	// immutable
	private Date addSeconds(Date in, int seconds) {
		return new Date(in.getTime() + (seconds * 1000));
	}

	public Object getValue(String key) {
		// is cache still valid?
		Date now = new Date();
		Date cacheExpirationDate = addSeconds(timestampOfLastDbAccess,
				this.cacheSeconds);
		boolean cacheExpired = now.after(cacheExpirationDate);
		boolean reloadCache = this.cache == null || cacheExpired;

		if (reloadCache) {
			this.cache = loadValues();
		}

		// now he hope to have the value
		return this.cache.get(key);
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

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}