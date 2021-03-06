package acp.db.connect.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
//import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionImpl;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
//import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.db.connect.IDbConnectHiber;

public class DbConnectHiber extends DbConnect implements IDbConnectHiber {
  private static Logger logger = LoggerFactory.getLogger(DbConnectHiber.class);

  private static SessionFactory sessionFactory;

  public DbConnectHiber() {
    dbPath = "/config/hiber";
    dbExt = "cfg.xml";
    dbDefaultName = "hibernate.cfg.xml";

    dbKeyIndex = "index";
    dbKeyName  = "name";
    dbKeyFullName  = "fullName";
    
    dbKeyUser = "hibernate.connection.username";
    dbKeyPassword = "hibernate.connection.password";
    dbKeyConnString = "hibernate.connection.url";
    dbKeyDriver = "hibernate.connection.driver_class";
  }
  
  @Override
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  @Override
  public boolean testConnect() {
    if (sessionFactory != null) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean connect() {
    logger.info("connecting ...");
    // ---------------------------
    if (sessionFactory == null) {
      if (dbProp != null) {
        sessionFactory = buildSessionFactory(dbProp.getProperty(dbKeyFullName));
      } else {
        sessionFactory = buildSessionFactory();
      }
    }
    // ---------------------------
    if (sessionFactory != null) {
      return true;
    } else {
      return false;
    }
    // ---------------------------
  }

  @Override
  public boolean connectDefault() {
    String dbIndex = null;
    String dbName = null;
    // ----------------------
    String[] list = getFileList();
    if (list.length == 0) {
      return false;
    }
    List<String> arrList = Arrays.asList(list);
    int ind = arrList.indexOf(dbDefaultName);
    if (ind >= 0) {
      dbIndex = String.valueOf(ind);
      dbName = dbDefaultName;
    } else {
      // return false;
      logger.info("???????????????????????? " + dbDefaultName + " ???? ??????????????. ???????????????????? ???????????? ???? ????????????.");
      dbIndex = "0";
      dbName = list[0];
    }
    // ----------------------
    Properties props = new Properties();
    props.setProperty(dbKeyIndex, dbIndex);
    props.setProperty(dbKeyName, dbName);
    // ----------------------
    readCfg(props);  // user, password
    setDbProp(props);
    // ----------------------
    boolean res = connect();
    // ----------------------
    return res;
  }

  @Override
  public boolean reconnect() {
    if (sessionFactory != null) {
      disconnect();
    }
    boolean res = connect();
    return res;
  }

  @Override
  public void disconnect() {
    logger.info("disconnecting ...");
    if (sessionFactory != null) {
      sessionFactory.close();
      sessionFactory = null;
    }
  }

  private SessionFactory buildSessionFactory() {
    return buildSessionFactory(null);
  }

  private SessionFactory buildSessionFactory(String cfg) {
    Configuration conf = configure(cfg);
    // ----------------------------------------------
    if (dbProp != null) {
      conf.setProperty(dbKeyUser, dbProp.getProperty(dbKeyUser));
      conf.setProperty(dbKeyPassword, dbProp.getProperty(dbKeyPassword));
    }
    // ----------------------------------------------
    try {
      SessionFactory sf = conf.buildSessionFactory();
      return sf;
    } catch (Throwable e) {
      logger.error(e.getMessage());
      return null;
    }
    // ----------------------------------------------
  }
 
  private Configuration configure(String cfg) {
    // ---------------------------------------------------------------------
    Configuration conf = new Configuration();
    if (cfg != null) {
      conf.configure(cfg);
    } else {
      conf.configure();
    }
    // printCfg(cfg,conf);
    return conf;
  }

  @Override
  public void readCfg(Properties props) {
    // ------------------------------------------------------
    String fullFileName = dbPath;
    if (dbPath != "") {
      fullFileName += "/";
    }
    fullFileName += props.getProperty(dbKeyName);
    props.setProperty(dbKeyFullName, fullFileName);
    // ------------------------------------------------------
    Configuration cnf = configure(fullFileName);
    // ------------------------------------------------------
    props.setProperty(dbKeyUser, cnf.getProperty(dbKeyUser));
    props.setProperty(dbKeyPassword, cnf.getProperty(dbKeyPassword));
    props.setProperty(dbKeyConnString,cnf.getProperty(dbKeyConnString));
    props.setProperty(dbKeyDriver,cnf.getProperty(dbKeyDriver));
    // ------------------------------------------------------
    printProps(props);
  }

  @SuppressWarnings("unused")
  private void printCfg(String cfg, Configuration conf) {
    System.out.println("");

    System.out.println("Configuration file = " + cfg);
    System.out.println(dbKeyConnString + " = " + conf.getProperty(dbKeyConnString));
    System.out.println(dbKeyDriver + " = " + conf.getProperty(dbKeyDriver));
    System.out.println(dbKeyUser + " = " + conf.getProperty(dbKeyUser));
    // System.out.println(dbKeyPassword + " = " + conf.getProperty(dbKeyPassword));
    System.out.println(dbKeyPassword + " = " + "****");

    System.out.println("");
  }

  private void printProps(Properties props) {
    System.out.println("");

    System.out.println("Configuration file = " + props.getProperty(dbKeyFullName));
    System.out.println(dbKeyConnString + " = " + props.getProperty(dbKeyConnString));
    System.out.println(dbKeyDriver + " = " + props.getProperty(dbKeyDriver));
    System.out.println(dbKeyUser + " = " + props.getProperty(dbKeyUser));
    // System.out.println(dbKeyPassword + " = " + props.getProperty(dbKeyPassword));
    System.out.println(dbKeyPassword + " = " + "****");

    System.out.println("");
  }

  @Override
  public String[] getFileList() {
    String[] list = {};
    URL url = getClass().getResource(dbPath);
    if (url == null) {
      return list;
    }
    String urlProtocol = url.getProtocol();
    System.out.println("getFileList:");
    System.out.println("url: " + url);
    System.out.println("urlProtocol: " + urlProtocol);
    
    if (urlProtocol.equalsIgnoreCase("jar")) {
      list = fileListJar(url,dbExt);
    } else {
      list = fileListFile(url,dbExt);
    }
    Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
    return list;
  }

  public String[] fileListFile(URL url, String ext) {
    URI uri = null;
    try {
      uri = url.toURI();
    } catch (URISyntaxException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }
    // ------------
    String[] list = {};
    if (uri != null) {
      File path = new File(uri);
      // list = path.list();
      list = path.list(filter(".*\\." + ext));
    }
    // ------------
    return list;
  }

  private FilenameFilter filter(final String regex) {
    return new FilenameFilter() {
      private Pattern pattern = Pattern.compile(regex);

      public boolean accept(File dir, String name) {
        return pattern.matcher(name).matches();
      }
    };
  }

  public String[] fileListJar(URL url, String ext) {
    String[] list = {};
    List<String> arrList = new ArrayList<>();
    JarURLConnection conn;
    try {
      conn = (JarURLConnection) url.openConnection();
      Enumeration<JarEntry> en = conn.getJarFile().entries();
      String mainEntryName = conn.getEntryName();
      while (en.hasMoreElements()) {
        JarEntry entry = en.nextElement();
        String entryName = entry.getName();
        if (entryName.startsWith(mainEntryName)) {
          if (!entry.isDirectory()) {
            String filter = "." + ext;
            if (entryName.endsWith(filter)) {
              int pos = entryName.lastIndexOf("/");
              String entryName2 = entryName.substring(pos+1, entryName.length());
              arrList.add(entryName2);
            }  
          }
        }
      }
      list = new String[arrList.size()];
      arrList.toArray(list);
    } catch (IOException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }
    return list;
  }

//  ------------------------------------------------------
//  Session. Transaction, Connection  
//------------------------------------------------------

  @Override
  public Session getCurrentSession() {
    Session session = sessionFactory.getCurrentSession();
    return session;
  }

  @Override
  public Session openSession() {
    Session session = sessionFactory.openSession();
    return session;
  }

  @Override
  public Session getSession() {
    Session session = sessionFactory.getCurrentSession();
//    Session session = sessionFactory.openSession();
    // -----------------------    
//    printSessionStatus(session);
    // -----------------------    
    // ????????????, ???????? ???????????? ?????????????????????? ?????????? sessionFactory.openSession
    // ???????? ?????????? sessionFactory.getCurrentSession, ???? ?????????????????? session.beginTransaction
    // ?????????? ???????????? ???? ??????????????.
    // -----------------------    
//    printConnections(session);  // !!!!! Only for OpenSession  !!!!
    // -----------------------    
    return session;
  }

  public Transaction getTransaction(Session sess) {
    Transaction tx = sess.beginTransaction();
//    printSessionStatus(sess);
//    printConnections(sess);
    return tx;
  }
  
  public void close(Session sess) {
    sess.close();
  }

  @Override
  public EntityManager getEntityManager() {
    EntityManager em = sessionFactory.createEntityManager();
    return em;
  }

  @Override
  public EntityTransaction getEntityTransaction(EntityManager em) {
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    return tx;
  }
  
  public void close(EntityManager em) {
    em.close();
  }

  @Override
  public Connection getConnectionBySF(SessionFactory sFactory) {
    // SessionFactoryImplementor sessionFactoryImpl = (SessionFactoryImplementor) sFactory;
    // ConnectionProvider connectionProvider = sessionFactoryImpl.getConnectionProvider();

    ConnectionProvider connectionProvider = sFactory.getSessionFactoryOptions().getServiceRegistry()
        .getService(ConnectionProvider.class);

    Connection conn = null;
    try {
      conn = connectionProvider.getConnection();
    } catch (SQLException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }
    return conn;
  }

  @Override
  public Connection getConnectionBySession(Session sess) {
    // Connection conn = sess.connection();  // ?????? ???? ????????????????
    // ------------------------------------------
//    SessionImpl sessionImpl = (SessionImpl) sess;  // ???? ???????????????? ?? getCurrentSession.
    SessionImpl sessionImpl = (SessionImpl) sess.unwrap(Session.class);
//    SessionImpl sessionImpl = sess.unwrap(SessionImpl.class);
    Connection conn = sessionImpl.connection();
    // ------------------------------------------
    return conn;
  }

  public Connection getConnectionDoWork(Session sess) {
    Connection conn = sess.doReturningWork(new ReturningWork<Connection>() {
      @Override
      public Connection execute(Connection p_conn) throws SQLException {
        return p_conn;
      }
    });
    return conn;
  }

  public void printConnectionDoWork(Session sess) {
    sess.doWork(new Work() {
      @Override
      public void execute(Connection p_conn) throws SQLException {
        System.out.println("p_conn: " + p_conn);
      }
    });
  }
  
  @SuppressWarnings("unused")
  private void printSessionStatus(Session sess) {
    System.out.println();
    
    logger.info("Session: " + sess);
    logger.info("Session isOpen: " + sess.isOpen());

    Transaction tx = sess.getTransaction();
    logger.info("Transaction:       " + tx);
    logger.info("Transaction isActive:       " + tx.isActive());
    logger.info("Transaction status:       " + tx.getStatus());
  }

  @SuppressWarnings("unused")
  private void printConnections(Session sess) {
    Connection conn1 = getConnectionBySession(sess);
    logger.info("Connection:       " + conn1);
    Connection conn2 = getConnectionDoWork(sess);
    logger.info("ConnectionDoWork: " + conn2);
    printConnectionDoWork(sess);
  }

}
