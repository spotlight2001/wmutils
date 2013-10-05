can have spring localized messages 
* from database
* caching configurable


doesnt work for "property"
* @Value("${...}")
* xml ... value="${bla}"

http://stackoverflow.com/questions/6246381/getting-localized-message-from-resourcebundle-via-annotations-in-spring-framewor



ultimate goal:
* @AutoWired Messages msg; + msg.get("myKey) -> value
* xml: ... <property name="..." value="${bla}" />
* @Value("${bla}"")


components:
* database + sql (configurable table, columns)
* some caching mechanism
	* @cache -> spring
	* map
	* ehcache
	* spring ReloadableResourceBundleMessageSource
* wiring into spel