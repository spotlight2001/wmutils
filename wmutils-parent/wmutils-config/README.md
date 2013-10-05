WHY?
can have 
* database configuration key|value
* automatic reload
* convention over configuration

---

final use:

annotation -> WORKS
@Value("#{dbcfg.MY.COOL.KEY}") ...

or

programmatically
@Autowired private ConfigInDbSource cfg;
...
cfg.getValueString("MY.COOL.KEY");

or 

xml -> !DOESNT RELOAD!
<property name="..." value="#{dbcfg.MY.COOL.KEY}" />

---

howto start:

include to your spring application context:
classpath*:/at/wm/wmutils/config/wmutils-config-sample-context.xml

---

requirements:

* 1x javax.sql.DataSource in application context (autodetected)
* table called "app_config", with column: "cfg_key" and "cfg_value"
(see \src\test\resources\at\wm\wmutils\config\db-schema.sql)

---

howto customize:
* create a classpath:/wmutils-config-custom.properties (autodetected)

whats configurable can be looked up in: src\main\resources\wmutils-config.properties

---
