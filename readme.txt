1) Все библиотеки переведены по возможности на последние версии, кроме logback.
   JDK 17, Eclipse 2021-12, Oracle драйверы ojdbc11 21.5, Hibernate 5.6.5, logback 1.2.5.
2) Устранены проблемы с несколькими connection.
3) Созданы классы работы с JPA, в том числе Criteria JPA.
Переход на новые версии библиотек завершен 10.02.2022

Начиная с версии logback 1.2.6 logger выдает исключение при подключении.
Проблема в xmlparserv2.jar. 
Logger создает SAXParser именно из Oracle-класса, а тот выдает исключение.

Parser configuration error occurred javax.xml.parsers.ParserConfigurationException: 
SAX feature 'http://xml.org/sax/features/external-general-entities' not supported.

Поэтому необходимо использовать максимум версию 1.2.5, если требуется xmlparserv2.jar

    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.2.5</version>
    </dependency>

    <dependency>
      <groupId>com.oracle.database.xml</groupId>
      <artifactId>xmlparserv2</artifactId>
      <version>${ojdbc.version}</version>
    </dependency>

