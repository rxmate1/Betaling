import java.sql.*;

public class MiniBillettApp {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/betaling"; // databasenavn
        String user = "root";                        // MySQL-bruker
        String password = "Betaling123";                       // MySQL-passord

        try {
            // 1️⃣ Koble til databasen
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Tilkobling OK!");

            // 2️⃣ Lag en ny bruker (hvis du vil teste)
            String sqlInsertUser = "INSERT INTO Bruker (navn, epost, passord_hash) VALUES (?, ?, ?)";
            PreparedStatement stmtUser = conn.prepareStatement(sqlInsertUser, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, "Kari Nordmann");
            stmtUser.setString(2, "kari@example.com");
            stmtUser.setString(3, "hashed_passord"); // simulerer passordhash
            stmtUser.executeUpdate();

            ResultSet generatedKeys = stmtUser.getGeneratedKeys();
            int brukerId = 0;
            if (generatedKeys.next()) {
                brukerId = generatedKeys.getInt(1);
            }

            System.out.println("Opprettet bruker med ID: " + brukerId);

            // 3️⃣ Lag en bestilling
            String sqlInsertBestilling = "INSERT INTO Bestilling (bruker_id, rute, avreise_tid, destinasjon, pris, status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtBestilling = conn.prepareStatement(sqlInsertBestilling);
            stmtBestilling.setInt(1, brukerId);
            stmtBestilling.setString(2, "Buss 24");
            stmtBestilling.setTimestamp(3, Timestamp.valueOf("2025-10-02 08:00:00"));
            stmtBestilling.setString(4, "Sentrum");
            stmtBestilling.setDouble(5, 45.00);
            stmtBestilling.setString(6, "PENDING"); // betaling ikke gjennomført ennå
            stmtBestilling.executeUpdate();

            System.out.println("Bestilling lagret (status PENDING)");

            // 4️⃣ Simuler betaling
            // Her kunne du kalle Stripe/Vipps API – for nå setter vi bare status til PAID
            String sqlUpdateStatus = "UPDATE Bestilling SET status = ? WHERE bruker_id = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateStatus);
            stmtUpdate.setString(1, "PAID");
            stmtUpdate.setInt(2, brukerId);
            stmtUpdate.executeUpdate();

            System.out.println("Betaling simulert – status oppdatert til PAID");

            // 5️⃣ Hent og vis bestillinger
            String sqlSelect = "SELECT b.bestilling_id, b.rute, b.destinasjon, b.pris, b.status, u.navn FROM Bestilling b JOIN Bruker u ON b.bruker_id = u.bruker_id";
            Statement stmtSelect = conn.createStatement();
            ResultSet rs = stmtSelect.executeQuery(sqlSelect);

            System.out.println("\nAlle bestillinger:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("bestilling_id") +
                        ", Bruker: " + rs.getString("navn") +
                        ", Rute: " + rs.getString("rute") +
                        ", Destinasjon: " + rs.getString("destinasjon") +
                        ", Pris: " + rs.getDouble("pris") +
                        ", Status: " + rs.getString("status"));
            }

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
