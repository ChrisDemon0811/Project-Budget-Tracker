package Project;

import java.util.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class ThuNhap_ChiTieu {
	private String category; // tên danh mục
	private double amount; // số tiền giao dịch
	private String type; // loại giao dịch: thu nhập, chi tiêu
	private LocalDateTime dateTime; // ngày + giờ giao dịch
	
	// constructor được gọi khi tạo giao dịch mới
	public ThuNhap_ChiTieu(String category, double amount, String type, LocalDateTime dateTime) {
		this.category = category;
		this.amount = amount;
		this.type = type;
		this.dateTime = dateTime;
	}

	// lấy thông tin đối tượng
	public String getCategory() {
		return category;
	}

	public double getAmount() {
		return amount;
	}

	public String getType() {
		return type;
	}
	public LocalDateTime getDateTime() { 
		return dateTime; 
	}

	// in đối tượng về dạng chuỗi
	@Override
	public String toString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
	    NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
	    String loai = type.equals("income") ? "Thu nhập" : "Chi tiêu";
	    return loai + " - " + category + " - " + currencyFormat.format(amount) + " VND - " + dateTime.format(formatter);
	}
}

class NganSach {
	private String category;
	private double limitAmount; // giới hạn chi tiêu cho danh mục đó

	// constructor để tạo danh sách mới cho danh mục
	public NganSach(String category, double limitAmount) {
		this.category = category;
		this.limitAmount = limitAmount;
	}

	// lấy thông tin về danh mục, giới hạn tiền đã đặt
	public String getCategory() {
		return category;
	}

	public double getLimitAmount() {
		return limitAmount;
	}
}

class BudgetTracker {
	// Tạo danh sách lưu tất cả các giao dịch đã được thực hiện
	private List<ThuNhap_ChiTieu> transactions = new ArrayList<>();
	// Tạo danh sách để lưu các bảng ghi chi tiêu
	private Map<String, NganSach> budgets = new HashMap<>();
	// Xác định loại tiền tệ (VNĐ)
	private final NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

	// Thêm giao dịch mới vào danh sách đã được tạo trước đó
	public void addTransaction(ThuNhap_ChiTieu t) {
		transactions.add(t);
	}

	public void setBudget(String category, double limit) {
		budgets.put(category.toLowerCase(), new NganSach(category, limit));
	}

	public void output() {
		double totalIncome = 0; // Tổng thu nhập
		double totalExpense = 0;// Tổng chi tiêu
		Map<String, Double> expensesByCategory = new HashMap<>();// Chi tiêu theo danh muc
		Map<String, Double> incomeByCategory = new HashMap<>();// Thu nhập theo danh mục

		// Tính toán thu nhập và chi tiêu
		for (ThuNhap_ChiTieu t : transactions) {
			if (t.getType().equals("income")) {
				totalIncome += t.getAmount();
				String cat = t.getCategory();
				incomeByCategory.put(cat, incomeByCategory.getOrDefault(cat, 0.0) + t.getAmount());

			} else if (t.getType().equals("expense")) {
				totalExpense += t.getAmount();
				String cat = t.getCategory();
				expensesByCategory.put(cat, expensesByCategory.getOrDefault(cat, 0.0) + t.getAmount());
			}
		}
		// In báo cáo
		System.out.println("\n---- BÁO CÁO ----");
		System.out.println("Tổng thu nhập: " + formatter.format(totalIncome) + " VND");
		for (String category : incomeByCategory.keySet()) {
			double amount = incomeByCategory.get(category);
			System.out.println("- " + category + ": " + formatter.format(amount) + " VND");
		}

		System.out.println("Tổng chi tiêu: " + formatter.format(totalExpense) + " VND");

		for (String category : expensesByCategory.keySet()) {
			double spent = expensesByCategory.get(category);
			System.out.print("- " + category + ": " + formatter.format(spent) + " VND");

			NganSach budget = budgets.get(category.toLowerCase());
			if (budget != null && spent > budget.getLimitAmount()) {
				System.out.println("Vượt ngân sách (" + formatter.format(budget.getLimitAmount()) + " VND)");
			} else {
				System.out.println();
			}
			double tienConLai = totalIncome - totalExpense;
			System.out.println("Tiền còn lại: " + formatter.format(tienConLai) + " VND");
		}
		System.out.println("\n--- Lịch sử giao dịch ---");
		for (ThuNhap_ChiTieu t : transactions) {
		    System.out.println(t);
		}
	}

	public boolean CanhBao(ThuNhap_ChiTieu t) {
		if (t.getType().equals("expense")) {
			String cat = t.getCategory().toLowerCase();

			// Tính tổng thu nhập và chi tiêu hiện tại
			double totalIncome = transactions.stream().filter(tr -> tr.getType().equals("income")).mapToDouble(ThuNhap_ChiTieu::getAmount).sum();

			double totalExpense = transactions.stream().filter(tr -> tr.getType().equals("expense")).mapToDouble(ThuNhap_ChiTieu::getAmount).sum();

			double soDuHienCo = totalIncome - totalExpense;

			// Kiểm tra số dư hiện tại
			if (t.getAmount() > soDuHienCo) {
				System.out.println("Giao dịch không thành công: Bạn không đủ tiền. Số dư hiện tại chỉ còn " + formatter.format(soDuHienCo) + " VND.");
				return false;
			}

			// Kiểm tra vượt ngân sách theo danh mục
			double spent = transactions.stream().filter(tr -> tr.getType().equals("expense") && tr.getCategory().equalsIgnoreCase(cat)).mapToDouble(ThuNhap_ChiTieu::getAmount).sum();
			double limit = budgets.containsKey(cat) ? budgets.get(cat).getLimitAmount() : Double.MAX_VALUE;

			if (spent + t.getAmount() > limit) {
				double nganSachConLai = limit - spent;
				System.out.println("Giao dịch không thành công vì chi tiêu '" + t.getCategory() + "' vượt quá ngân sách (" + formatter.format(limit) + " VND)");
				System.out.println("Ngân sách bạn còn: " + formatter.format(nganSachConLai) + " VND");
				return false;
			}
		}

		transactions.add(t);
		return true;
	}

}

public class Project {
	// Tạo lớp chứa chương trình Project
	public static void main(String[] args) {
		// Điểm mở đầu của chương trình
		BudgetTracker tracker = new BudgetTracker();
		// Tạo công cụ theo dõi ngân sách(budget tracker)
		Scanner scanner = new Scanner(System.in);
		// Tạo công cụ đọc dữ liệu người dùng từ bàn phím

		while (true) {
			// Tạo vòng lặp vô hạn để hiển thị menu và xử lý yêu cầu liên tục
			System.out.println("\n--- Trình theo dõi ngân sách ---");
			System.out.println("1. Thêm giao dịch");
			System.out.println("2. Thiết lập ngân sách");
			System.out.println("3. Xem báo cáo");
			System.out.println("4. Thoát");
			// Hiển thị các lựa chọn
			System.out.print("Chọn tùy chọn: ");
			// Nhắc nhở người dùng nhập số liệu
			int choice = scanner.nextInt();
			// Đọc số nguyên
			scanner.nextLine();
			// Loại bỏ kí tự /n

			switch (choice) {
			// Xử lí từng case theo giá trị choice
			case 1 -> {
				// Thêm giao dịch
				System.out.println("Loại giao dịch:");
				System.out.println("1. Thu nhập");
				System.out.println("2. Chi tiêu");
				System.out.print("Chọn 1 hoặc 2: ");
				int typeChoice = scanner.nextInt();
				// đọc số nguyên
				scanner.nextLine();
				String type = (typeChoice == 1) ? "income" : "expense";
				// Xác định chuỗi giao dịch
				System.out.print("Danh mục: ");
				String category = scanner.nextLine();
				// đọc danh mục
				System.out.print("Số tiền: ");
				double amount = scanner.nextDouble();
				// đọc số thực
				scanner.nextLine();
				LocalDateTime now = LocalDateTime.now();
				boolean success = tracker.CanhBao(new ThuNhap_ChiTieu(category, amount, type, now));
				// Tạo đối tượng ThuNhap_ChiTieu(category, amount, type) truyền cho tracker
				if (success) {
					System.out.println("Giao dịch thành công.");
				}
				// nếu success thì in ra giao dịch thành công

			}
			case 2 -> {
				System.out.print("Danh mục: ");
				String category = scanner.nextLine();
				// Đọc danh mục đạt hạn mức
				System.out.print("Giới hạn chi tiêu: ");
				double limit = scanner.nextDouble();
				// Giới hạn chi tiêu
				scanner.nextLine();
				tracker.setBudget(category, limit);
				// Lưu hạn mức
				System.out.println("Ngân sách đã thiết lập.");
			}
			case 3 -> tracker.output();
			// Hiển thị kết quả của case 1 và case 2
			case 4 -> {
				System.out.println("Tạm biệt!");
				return;
			}
			// Kết thúc main
			default -> System.out.println("Lựa chọn không hợp lệ.");
			// in ra khi người dùng nhập số ngoài 1 2 3 4
			}
		}
	}
}
