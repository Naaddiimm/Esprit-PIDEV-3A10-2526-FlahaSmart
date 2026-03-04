public class TestDB {
    public static void main(String[] args) {
        utilies.MyDataBase db = utilies.MyDataBase.getInstance();
        if (db.getConnection() != null) {
            System.out.println("Database connection is working!");
        } else {
            System.out.println("Failed to connect to the database.");
        }
    }
}
