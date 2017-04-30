package com.company;

import java.sql.*;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * Created by Francis on 3/11/2017.
 * Reference: for hashing password http://docs.spring.io/spring-security/site/docs/current/apidocs/org/springframework/security/crypto/bcrypt/BCrypt.html
 */

/*REFERENCES:
* https://www.tutorialspoint.com/jdbc/jdbc-sample-code.htm
* http://zetcode.com/db/mysqljava/
* http://www.vogella.com/tutorials/MySQLJava/article.html#jdbcdriver
*/

public class mySQLDB {

    //variable to use JDBC driver and creates a database url name chessmaster.db
    private static final String JDBC_DRIVER = "org.sqlite.JDBC";
    private static final String DB_URL = "jdbc:sqlite:chessmaster.db";

    private Connection conn; //to connect to the database
    private PreparedStatement pstmt; //preparedStatement for sql statements
    private ResultSet res; //acts as a cursor to traverse data in datbase

    //set up JDBC_driver
    public mySQLDB() {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //Function to initialise conn to getConnection
    public void getConnection() {
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //Function to createTable account and profile
    public void createTable() {
        try {
            //calls function connection to database
            getConnection();
            //create preparedStatement
            pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS account(ID INTEGER PRIMARY KEY AUTOINCREMENT, username VARCHAR(20) NOT NULL UNIQUE, password VARCHAR(20) NOT NULL)");
            pstmt.executeUpdate(); //runs/execute pstmt
            pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS profile(userID INTEGER PRIMARY KEY AUTOINCREMENT, rank int(11) NOT NULL, win int(11) NOT NULL, loss int(11) NOT NULL, coins int(11) NOT NULL, username varchar(20) NOT NULL, FOREIGN KEY(username) REFERENCES account(username))");
            pstmt.executeUpdate();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println(e);
        } finally {
            //Calls function to close connection and resources
            closeRsc();
        }
    }

    //Function for user login
    public boolean Login(String username, String password) {
        try {
            getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM account WHERE username = ?");
            pstmt.setString(1, username);
            res = pstmt.executeQuery();
            //validation of  user account
            if (res.next()) { //if username is valid
                String hashPW = res.getString("password");
                if ((BCrypt.checkpw(password, hashPW))) {  //if user password match the hashpassword in database
                    return true;
                } else {
                    return false;
                }

            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            closeRsc();
        }

    }

    //Method to create/register account and profile for user /create data of user
    public boolean insertData(String username, String password) {
        try {
            getConnection();
            String pw_hash = BCrypt.hashpw(password, BCrypt.gensalt()); //hash/BCrypt the password
            pstmt = conn.prepareStatement("INSERT INTO account(username,password) VALUES(?,?)");         //create account statement
            pstmt.setString(1, username);
            pstmt.setString(2, pw_hash);
            int row = pstmt.executeUpdate(); //validate if registration account succeed return row greater than 0
            if (row > 0) {
                //create profile, user details initialise as 0
                int rank = 0;
                int win = 0;
                int loss = 0;
                int coins = 0;
                String skins = "";
                pstmt = conn.prepareStatement("INSERT INTO profile(rank,win,loss,coins,username) VALUES(?,?,?,?,?)");
                pstmt.setInt(1, rank);
                pstmt.setInt(2, win);
                pstmt.setInt(3, loss);
                pstmt.setInt(4, coins);
                pstmt.setString(5, username);
//                pstmt.setString(6, skins);
                row = pstmt.executeUpdate();                 //validate profile creation, return row greater than 0
                if (row > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            //System.out.println(e);
            return false;
        } finally {
            closeRsc();
        }
    }

    //Method to view selected user profile /read data of user
    public String[] viewProfile(String username) {
        try {
            getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM profile WHERE username = ?");
            pstmt.setString(1, username);
            res = pstmt.executeQuery();
            //Extract data from result set
            if (res.next()) {
                System.out.println(username + " profile");
                //Retrieve by column name and stored in variables
                int rank = res.getInt("rank");
                int win = res.getInt("win");
                int loss = res.getInt("loss");
                int coins = res.getInt("coins");
//                String skins = res.getString("skins");
                return new String[]{username, String.valueOf(rank), String.valueOf(win), String.valueOf(loss), String.valueOf(coins)};
            } else {
                return new String[]{""};
            }
        } catch (Exception e) {
            return new String[]{""};
        } finally {
            closeRsc();
        }
    }

    //Function to update Win or loss of the players
    public void updateWinLoss(boolean result, String username) {
        try {
            int win = 0;
            int loss = 0;

            getConnection();
            pstmt = conn.prepareStatement("SELECT win,loss FROM profile WHERE username = ?"); //statement to get user win and loss
            pstmt.setString(1, username);
            res = pstmt.executeQuery();
            if (res.next()) {
                win = res.getInt("win");
                loss = res.getInt("loss");
            }

            if (result) { //if result = true = win
                pstmt = conn.prepareStatement("UPDATE profile SET win = ? WHERE username = ?");
                win = win + 1;
                pstmt.setInt(1, win);
                pstmt.setString(2, username);
            } else { //if result = false = loss
                pstmt = conn.prepareStatement("UPDATE profile SET loss = ? WHERE username = ?");
                loss = loss + 1;
                pstmt.setInt(1, loss);
                pstmt.setString(2, username);
            }
            int row = pstmt.executeUpdate();

            if (row > 0) {
                System.out.println(username+" Update Win or Loss Complete");
            } else {
                System.out.println("Update error");
            }

        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            closeRsc();
        }
    }

    //Function to update all rank of the players
    public void updataRank() {
        try {
            Statement stmt; //variable for a statement
            getConnection();
            float winpercentage;
            int rank = 0;
            float temp = 0;
            String user;
            pstmt = conn.prepareStatement("UPDATE profile SET rank = ? WHERE username = ?");
            stmt = conn.createStatement();
            ResultSet ursRank = stmt.executeQuery("select username,(1.0*win/(win+loss))as winpercent from profile order by winpercent Desc;"); //query to set new rank of each players base on winning percentage
            // updates all the rank of players by winning percentage //inefficient with larger database
            while (ursRank.next()) {
                winpercentage = ursRank.getFloat("winpercent");
                user= ursRank.getString("username");
                if (winpercentage != temp) { //update rank by from highest win percentage
                    temp = winpercentage;
                    rank = rank+1;
                    pstmt.setInt(1, rank);
                    pstmt.setString(2, user);
                    int row = pstmt.executeUpdate();
                    if (row > 0) {
                        System.out.println(user+" Update Complete");
                    } else {
                        System.out.println("Update error");
                    }
                } else { //if two or more user winpercentage are the same they will share rank
                    temp = winpercentage;
                    pstmt.setInt(1, rank);
                    pstmt.setString(2, user);
                    int row = pstmt.executeUpdate();
                    if (row > 0) {
                        System.out.println(user+" Update Complete shared rank");
                    } else {
                        System.out.println("Update error");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            closeRsc();
        }
    }


    //Method to delete account and profile /delete data
    public void deleteData(String username) {
        //if account is deleted  profile should be deleted too!
        try {
            getConnection();
            //deletes profile
            pstmt = conn.prepareStatement("DELETE FROM profile WHERE username = ?");
            pstmt.setString(1, username);
            int row = pstmt.executeUpdate();
            if (row > 0) {
                System.out.println("Deleted Profile");

                //deletes account
                pstmt = conn.prepareStatement("DELETE FROM account WHERE username= ?");
                pstmt.setString(1, username);
                row = pstmt.executeUpdate();
                if (row > 0) {
                    System.out.println("Deleted account");
                } else {
                    System.out.println("error");
                }
            } else {
                System.out.println("error");
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            closeRsc();
        }
    }

    //Close database resources
    private void closeRsc() {
        try {
            if (res != null) {
                res.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
}
