1) ��� ���������� ���������� �� ����������� �� ��������� ������, ����� logback.
   JDK 17, Eclipse 2021-12, Oracle �������� ojdbc11 21.5, Hibernate 5.6.5, logback 1.2.5.
2) ��������� �������� � ����������� connection.
3) ������� ������ ������ � JPA, � ��� ����� Criteria JPA.
������� �� ����� ������ ��������� �������� 10.02.2022

������� � ������ logback 1.2.6 logger ������ ���������� ��� �����������.
�������� � xmlparserv2.jar. 
Logger ������� SAXParser ������ �� Oracle-������, � ��� ������ ����������.

Parser configuration error occurred javax.xml.parsers.ParserConfigurationException: 
SAX feature 'http://xml.org/sax/features/external-general-entities' not supported.

������� ���������� ������������ �������� ������ 1.2.5, ���� ��������� xmlparserv2.jar

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

