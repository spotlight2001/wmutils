package at.wm.wmutils.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

public class DatabaseResourceLoader implements ResourceLoader {

	private DataSource dataSource;

	private String databaseTableName;

	private String databaseKeyColumn;

	private String databaseValueColumn;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDatabaseTableName(String databaseTableName) {
		this.databaseTableName = databaseTableName;
	}

	public void setDatabaseKeyColumn(String databaseKeyColumn) {
		this.databaseKeyColumn = databaseKeyColumn;
	}

	public void setDatabaseValueColumn(String databaseValueColumn) {
		this.databaseValueColumn = databaseValueColumn;
	}

	public Resource getResource(String location) {
		if (location.startsWith("db:")) {

			return new AbstractResource() {
				@Override
				public String getFilename() {
					return databaseTableName;
				}

				@Override
				public boolean exists() {
					// TODO implementation
					return true;
				}

				public InputStream getInputStream() throws IOException {

					// TODO impl streaming
					PipedInputStream in = new PipedInputStream();
					final PipedOutputStream out = new PipedOutputStream(in);

					// fill data
					final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
					final String sql = String.format(
							"select %s key, %s value from %s order by 1",
							databaseKeyColumn, databaseValueColumn,
							databaseTableName);
					RowCallbackHandler rowCallbackHandler = new RowCallbackHandler() {
						public void processRow(ResultSet rs)
								throws SQLException {
							String key = rs.getString("key");
							String value = rs.getString("value");
							// property format like "app.config.bla=xyz"
							String line = key + "=" + value + "\r\n";
							try {
								out.write(line.getBytes());
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					};
					jdbc.query(sql, rowCallbackHandler);
					out.close();

					return in;
				}

				public String getDescription() {
					return "i can load from db";
				}

			};
		} else {
			return new DefaultResourceLoader().getResource(location);
		}
	}

	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}
}