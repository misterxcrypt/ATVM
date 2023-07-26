import java.sql.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class Main {

	public static void main(String[] args) {
		try (Scanner sc = new Scanner(System.in)) {
			//		Class.forName("com.mysql.jdbc.Driver");
			try {

			String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$";
			System.out.println("Welcome to our Automatic Ticket Vending Machine\n-------------------------------------------------------------------");
			
			//user details
			System.out.println("Your Name: ");
			String Name = sc.nextLine();
			System.out.println("Your Age: ");
			int age = sc.nextInt();
			sc.nextLine();
			System.out.println("Your Gender: ");
			String Gender = sc.nextLine();
			int emailvalid = 0;
			System.out.println("Your Email: ");
			String email = sc.nextLine();
			if(!email.matches(emailRegex)) {
				System.out.println("Entered email is wrong");
			}
			else {emailvalid = 1;}
			
			
			//get destination & date to make query
			System.out.println("Source: Coimbatore Junction");
			System.out.println("Destination: ");
			String Destination = sc.nextLine();
			System.out.println("Date (Format: YYYY-MM-DD): ");
			String date = sc.nextLine();
			
			System.out.println("\n");
			
			//query
		    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ATVM","root","Mysqlroot3306#");
			Statement stm = con.createStatement();
			String str = "Select * from Train_details where Destination='"+Destination+"' AND DepartureDate='"+date+"'";
			ResultSet rs = stm.executeQuery(str);
			
			if (rs.next() == false) {
			      System.out.println("No trains available at that date");
			      return ;
			}
			
			do {
				System.out.println(rs.getString(1)+"\t"+rs.getString(2)+"\t"+rs.getString(3)+"\t"+rs.getString(4)+"\t"+rs.getString(5)+"\t"+rs.getString(6)+"\t"+rs.getString(7)+"\t"+rs.getString(8)+"\t"+rs.getString(9)+"\t"+rs.getString(10)+"\t"+rs.getString(11)+"\t"+rs.getString(12)+"\t"+rs.getString(13));
			}while(rs.next());

			//Select any train
			System.out.println("\nSelect any one Trip: ");
			int sno = sc.nextInt();
			
			//check tickets availability
			String str2 = "Select Tickets_left from Train_details where SNo="+sno; 
			ResultSet rs2 = stm.executeQuery(str2);
			System.out.println("How many tickets: ");
			int Ticket_need = sc.nextInt();
			while(rs2.next()) {
				int tickets = rs2.getInt(1);
				if(tickets == 0 || tickets<Ticket_need) {
					System.out.println("\nTickets Insufficient");
					return ;
				}
				else {
					System.out.println("\n"+tickets+" tickets available\n");
				}
			}
			
			//get amount
			String str3 = "Select Amount from Train_details where Sno="+sno;
			ResultSet rs3 = stm.executeQuery(str3);
			while(rs3.next()) {
				int amount = rs3.getInt(1);
				int platform = 10;
				int Total = (Ticket_need*amount) + (Ticket_need*platform);
				System.out.println("Ticket amount: "+(Ticket_need*amount)+"\nPlatform ticket: "+(Ticket_need*platform)+"\nTotal: "+Total);
				
				//Payment through 
				System.out.println("\nPayment through\n1. Smart Card\n2. QR code");
				System.out.println("Choose a option: ");
				int option = sc.nextInt();
				switch(option) {
				case 1:
					Smartcard(Total);
					break;
				case 2:
					System.out.println("Under development");
					break;
				default:
					System.out.println("Choose valid option");
				}
			}
			
			//ticket update
			String str5 = "Select Tickets_left from Train_details where SNo="+sno; 
			ResultSet rs5 = stm.executeQuery(str5);
			rs5.next();
				int tickets = rs5.getInt(1);
				tickets = tickets - Ticket_need;
				String str4 = "UPDATE Train_details SET Tickets_left="+tickets+" where SNo="+sno; 
				stm.executeUpdate(str4);
			
//			tickets = tickets - Ticket_need;
//			String str4 = "UPDATE Train_details SET tickets="+tickets+" where SNo="+sno; 
//			stm.executeUpdate(str4);
//			while(rs4.next()) {
//				int tickets = rs4.getInt(1);
//				tickets = tickets-Ticket_need;
//				String updation = "UPDATE Train_details SET tickets="+tickets+" where SNo="+sno;
//				stm.execute(updation);
//			}
			
			
//			Email(email, emailvalid, Name, age, Gender);
			
			}
			catch(Exception e) {
				System.out.println(e);
			}
		}
	}
	static void Smartcard(int Total) {
		Scanner sc = new Scanner(System.in);
		System.out.println("\nYour Smart card id:");
		String id = sc.nextLine();
		
		//check smartcard existence
		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ATVM","root","Mysqlroot3306#");
			Statement stm = con.createStatement();
			String str = "Select * from smartcard where ID='"+id+"'";
			ResultSet rs = stm.executeQuery(str);
			if(rs.next()==false) {
				System.out.println("No smartcard detail is available");	
				return;
			}
			else {System.out.println("\nhello "+rs.getString("Name")+"! Your Smartcard details available");}
			
		//security Authentication
			String str1 = "Select pin from smartcard where ID='"+id+"'";
			ResultSet rs1 = stm.executeQuery(str1);
			rs1.next();
			int originalpin = rs1.getInt("pin");
			System.out.println("Enter pin:");
			int userpin = sc.nextInt();
			if(originalpin == userpin)
			{
				System.out.println("\nPin is correct");
			}
			else {System.out.println("\nPin is wrong"); return;}
		
		//get Balance
			String str2 = "Select Balance from smartcard where ID='"+id+"'";
			ResultSet rs2 = stm.executeQuery(str2);
			while(rs2.next()) {
				int Balance = rs2.getInt(1);
				if(Balance <= 50) {
					System.out.println("\nBalance Amount is lower than minimum balance: "+Balance);
					return;
				}
				if(Balance <= Total+50) {
					System.out.println("Low Balance");
					return;
				}
				Balance = Balance - Total; 
				String updation = "UPDATE smartcard SET Balance="+Balance+" where id="+id;
				stm.execute(updation);
				System.out.println("Your Balance is: "+Balance);
				System.out.println("Your ticket is booked. Check your mail");
			}
		}
		catch(Exception e) {}
		
		sc.close();
	}
//	static void Email(String email, int emailvalid, String Name, int age, String Gender) {
//		// email ID of Recipient.
//		String recipient = "mailrecovering4@gmail.com";
//		
//		// email ID of  Sen
//		String sender = "haribrothers6@gmail.com";
//	
//		String host = "localhost";
//		Properties properties = System.getProperties();
//		properties.setProperty("mail.smtp.host", host);
//		Session session = Session.getDefaultInstance(properties);
//		
//		try
//			{
//			MimeMessage message = new MimeMessage(session);
//		
//			message.setFrom(new InternetAddress(sender));message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
//			message.setSubject("This is Subject");
//			message.setText("This is a test mail");
//			Transport.send(message);
//			System.out.println("Mail successfully sent");
//			}
//		catch (MessagingException mex){
//			mex.printStackTrace();
//		}
//	}
}