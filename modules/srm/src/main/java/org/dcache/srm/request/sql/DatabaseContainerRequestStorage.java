/*
 * FileRequestStorage.java
 *
 * Created on June 17, 2004, 3:18 PM
 */

package org.dcache.srm.request.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.dcache.srm.SRMInvalidRequestException;
import org.dcache.srm.SRMUser;
import org.dcache.srm.request.ContainerRequest;
import org.dcache.srm.request.FileRequest;
import org.dcache.srm.request.Job;
import org.dcache.srm.request.Request;
import org.dcache.srm.util.Configuration;

/**
 *
 * @author  timur
 */
public abstract class DatabaseContainerRequestStorage extends DatabaseRequestStorage {
   private final static Logger logger =
            LoggerFactory.getLogger(DatabaseContainerRequestStorage.class);


    /** Creates a new instance of DatabaseContainerRequestStorage */
    public DatabaseContainerRequestStorage(Configuration.DatabaseParameters configuration) throws SQLException {
        super(configuration);
    }

   public abstract String getFileRequestsTableName();
   /*{
        return getTableName()+"_filerequestids";
    }
    **/

    public abstract void dbInit1() throws SQLException;

    @Override
    protected void _dbInit() throws SQLException {
        dbInit1();

    }


    protected abstract ContainerRequest getContainerRequest(
    Connection _con,
    Long ID,
    Long NEXTJOBID,
    long CREATIONTIME,
    long LIFETIME,
    int STATE,
    String ERRORMESSAGE,
    SRMUser user,
    String SCHEDULERID,
    long SCHEDULER_TIMESTAMP,
    int NUMOFRETR,
    int MAXNUMOFRETR,
    long LASTSTATETRANSITIONTIME,
    Long CREDENTIALID,
    int RETRYDELTATIME,
    boolean SHOULDUPDATERETRYDELTATIME,
    String DESCRIPTION,
    String CLIENTHOST,
    String STATUSCODE,
    FileRequest[] fileRequests,
    ResultSet set,
    int next_index)throws SQLException;

    @Override
    protected Request
    getRequest(
    Connection _con,
    Long ID,
    Long NEXTJOBID,
    long CREATIONTIME,
    long LIFETIME,
    int STATE,
    String ERRORMESSAGE,
    SRMUser user,
    String SCHEDULERID,
    long SCHEDULER_TIMESTAMP,
    int NUMOFRETR,
    int MAXNUMOFRETR,
    long LASTSTATETRANSITIONTIME,
    Long CREDENTIALID,
    int RETRYDELTATIME,
    boolean SHOULDUPDATERETRYDELTATIME,
    String DESCRIPTION,
    String CLIENTHOST,
    String STATUSCODE,
    ResultSet set,
    int next_index) throws SQLException {

        String sqlStatementString = "SELECT ID FROM " + getFileRequestsTableName() +
        " WHERE RequestID="+ID;
        Statement sqlStatement = _con.createStatement();
        logger.debug("executing statement: "+sqlStatementString);
        ResultSet fileIdsSet = sqlStatement.executeQuery(sqlStatementString);
        Set<Long> utilset = new HashSet<>();
        while(fileIdsSet.next()) {
            utilset.add(fileIdsSet.getLong(1));
        }
        fileIdsSet.close();
        sqlStatement.close();

        Long [] fileIds = utilset.toArray(new Long[utilset.size()]);
        sqlStatement.close();
        FileRequest[] fileRequests = new FileRequest[fileIds.length];
        for(int i = 0; i<fileRequests.length; ++i) {
            try {
                fileRequests[i] = Job.getJob(fileIds[i], FileRequest.class, _con);
            } catch (SRMInvalidRequestException ire){
                logger.error(ire.toString());
            }
        }
        return getContainerRequest(
        _con,
        ID,
        NEXTJOBID ,
        CREATIONTIME,
        LIFETIME,
        STATE,
        ERRORMESSAGE,
        user,
        SCHEDULERID,
        SCHEDULER_TIMESTAMP,
        NUMOFRETR,
        MAXNUMOFRETR,
        LASTSTATETRANSITIONTIME,
        CREDENTIALID,
        RETRYDELTATIME,
        SHOULDUPDATERETRYDELTATIME,
        DESCRIPTION,
        CLIENTHOST,
        STATUSCODE,
        fileRequests,
        set,
        next_index );
    }

    @Override
    public abstract String getTableName();


    public abstract  void getCreateList(ContainerRequest cr,StringBuffer sb);

    @Override
    public void getCreateList(Request r, StringBuffer sb) {

        if(r == null || !(r instanceof ContainerRequest)) {
            throw new IllegalArgumentException("Request is not ContainerRequest" );
        }
        ContainerRequest cr = (ContainerRequest)r;

        getCreateList(cr,sb);

    }

}
