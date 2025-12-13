package com.ptudn12.main.utils;

import java.util.ArrayList;
import java.util.List;

public class NumberUtils {
	private static final String[] CHU_SO = { "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };
	private static final String[] DON_VI_LON = { "", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ" };

	public static String docSoThanhChu(long so) {
		if (so == 0)
			return "Không đồng";
		if (so < 0)
			return "Âm " + docSoThanhChu(-so);

		// 1. Chia số thành các cụm 3 số từ PHẢI sang TRÁI
		List<Integer> cacCum = new ArrayList<>();
		String s = String.valueOf(so);
		while (s.length() > 0) {
			int indexCat = Math.max(0, s.length() - 3);
			cacCum.add(Integer.parseInt(s.substring(indexCat))); // Thêm vào danh sách
			s = s.substring(0, indexCat);
		}

		// 2. Duyệt từ cụm cao nhất (cuối danh sách) về cụm thấp nhất
		StringBuilder ketQua = new StringBuilder();
		boolean coCumTruocDo = false; // Đánh dấu xem đã đọc cụm nào trước đó chưa

		for (int i = cacCum.size() - 1; i >= 0; i--) {
			int giaTriCum = cacCum.get(i);
			boolean laCumCaoNhat = (i == cacCum.size() - 1);

			// Quy tắc: nếu cụm toàn 0 thì bỏ qua, trừ khi nó là cụm duy nhất (đã xử lý ở
			// đầu hàm)
			if (giaTriCum == 0) {
				continue;
			}

			// Xử lý đọc nội dung của cụm (0-999)
			String textCum = docCumBaSo(giaTriCum, laCumCaoNhat);

			// Ghép vào kết quả
			if (ketQua.length() > 0)
				ketQua.append(" ");
			ketQua.append(textCum);

			// Thêm đơn vị lớn (nghìn, triệu...)
			ketQua.append(" ").append(DON_VI_LON[i]);

			coCumTruocDo = true;
		}

		// 3. Hoàn thiện chuỗi
		String rs = ketQua.toString().trim();
		// Xóa khoảng trắng thừa nếu có
		rs = rs.replaceAll("\\s+", " ");
		// Viết hoa chữ cái đầu + thêm " đồng"
		return rs.substring(0, 1).toUpperCase() + rs.substring(1) + " đồng";
	}

	/**
	 * Đọc một cụm 3 số (ví dụ: 088, 123, 5)
	 * 
	 * @param n            Giá trị của cụm
	 * @param laCumCaoNhat True nếu đây là cụm đầu tiên bên trái (để bỏ qua 'không
	 *                     trăm')
	 */
	private static String docCumBaSo(int n, boolean laCumCaoNhat) {
		int tram = n / 100;
		int chuc = (n % 100) / 10;
		int donvi = n % 10;

		StringBuilder sb = new StringBuilder();

		// --- XỬ LÝ HÀNG TRĂM ---
		if (laCumCaoNhat && tram == 0) {
			// Nếu là cụm cao nhất và trăm = 0 (ví dụ số 88 hoặc 5), bỏ qua đọc trăm
		} else {
			// Các trường hợp còn lại luôn đọc trăm (kể cả 0 trăm)
			sb.append(CHU_SO[tram]).append(" trăm");
		}

		// --- XỬ LÝ HÀNG CHỤC ---
		if (chuc == 0) {
			// Nếu hàng chục là 0 và hàng đơn vị > 0, đọc là "linh"
			// Điều kiện: Phải có hàng trăm đi trước (ví dụ 105 -> một trăm linh năm)
			// Hoặc nếu không phải cụm cao nhất (ví dụ ..triệu không trăm linh năm..)
			if (donvi > 0) {
				if (tram > 0 || !laCumCaoNhat) {
					sb.append(" linh");
				}
			}
		} else if (chuc == 1) {
			sb.append(" mười");
		} else {
			sb.append(" ").append(CHU_SO[chuc]).append(" mươi");
		}

		// --- XỬ LÝ HÀNG ĐƠN VỊ ---
		if (donvi > 0) {
			if (chuc == 0 || chuc == 1) {
				// Trường hợp chục = 0 (linh 5) hoặc chục = 1 (mười 5)
				sb.append(" ").append(CHU_SO[donvi]);
			} else {
				// Trường hợp chục > 1 (hai mươi ...)
				if (donvi == 1)
					sb.append(" mốt");
				else if (donvi == 4)
					sb.append(" tư");
				else if (donvi == 5)
					sb.append(" lăm");
				else
					sb.append(" ").append(CHU_SO[donvi]);
			}
		} else {
			// Đơn vị = 0, không đọc gì thêm (ví dụ: 100, 120)
			// Trừ khi cụm chỉ có đúng 1 số 0 (đã xử lý ở hàm cha)
		}

		// Xử lý đặc biệt cho hàng đơn vị khi chục = 1 (mười một, mười lăm)
		// Logic trên: " mười" + " " + "năm" -> "mười năm" (sai, phải là lăm)
		// Fix lại đoạn logic đơn vị cho chục = 1:
		if (chuc == 1 && donvi == 1) {
			// "mười một" - đúng logic trên (CHU_SO[1] là một)
		}
		if (chuc == 1 && donvi == 5) {
			// Cần sửa thành "lăm"
			int len = sb.length();
			// Xóa chữ "năm" nếu lỡ append ở trên (logic trên add CHU_SO[5] là năm)
			// Để code clean hơn, ta viết lại logic đơn vị tách biệt rõ ràng:
		}

		// --- VIẾT LẠI LOGIC ĐƠN VỊ CHO GỌN VÀ CHÍNH XÁC ---
		// Reset SB để ghép lại từ đầu phần Đuôi cho chắc chắn
		sb.setLength(0); // Xóa làm lại đoạn text

		// 1. Trăm
		if (laCumCaoNhat && tram == 0) {
			// Bỏ qua
		} else {
			sb.append(CHU_SO[tram]).append(" trăm");
		}

		// 2. Chục & Đơn vị
		if (chuc == 0 && donvi == 0) {
			// Chỉ có trăm (100, 200) -> Xong
		} else if (chuc == 0 && donvi > 0) {
			// 05 -> linh năm
			// Chỉ đọc "linh" nếu có trăm phía trước hoặc không phải cụm cao nhất
			if (tram > 0 || !laCumCaoNhat) {
				sb.append(" linh ").append(CHU_SO[donvi]);
			} else {
				sb.append(" ").append(CHU_SO[donvi]); // Trường hợp số 5
			}
		} else if (chuc == 1) {
			// 10-19
			sb.append(" mười");
			if (donvi == 1)
				sb.append(" một");
			else if (donvi == 5)
				sb.append(" lăm");
			else if (donvi > 0)
				sb.append(" ").append(CHU_SO[donvi]);
		} else {
			// 20-99
			sb.append(" ").append(CHU_SO[chuc]).append(" mươi");
			if (donvi == 1)
				sb.append(" mốt");
			else if (donvi == 4)
				sb.append(" tư");
			else if (donvi == 5)
				sb.append(" lăm");
			else if (donvi > 0)
				sb.append(" ").append(CHU_SO[donvi]);
		}

		return sb.toString().trim();
	}
}