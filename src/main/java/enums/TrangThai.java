package enums;


public enum TrangThai {
    DA_DAT("Đã đặt"),
    DA_HUY("Đã hủy"),
    DANG_CHO("Đang chờ"),
    DA_THANH_TOAN("Đã thanh toán");

    private final String description;

    TrangThai(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}