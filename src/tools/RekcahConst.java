package tools;


public interface RekcahConst {
	/** 无翻转 */
	public static final int FLIP_NONE = 0;
	/** 水平翻转 */
	public static final int FLIP_X = 1;
	/** 垂直翻转 */
	public static final int FLIP_Y = 1 << 1;
	/** 90度旋转 */
	public static final int FLIP_90 = 1 << 2; // 4
	/** 水平翻转 + 垂直翻转 等价于 ( FLIP_X | FLIP_Y ) */
	public static final int FLIP_X_Y = FLIP_X | FLIP_Y;// 3
	/** 90度旋转 + 水平翻转 等价于 ( FILP_90 | FLIP_X ) */
	public static final int FLIP_90_X = FLIP_90 | FLIP_X;// 5 Y?
	/** 90度旋转 + 垂直翻转 等价于 ( FILP_90 | FLIP_Y ) */
	public static final int FLIP_90_Y = FLIP_90 | FLIP_Y;// 6 X?
	/** 90度旋转 + 水平翻转 + 垂直翻转 等价于 ( FILP_90 | FLIP_X | FLIP_Y ) */
	public static final int FLIP_90_X_Y = FLIP_90 | FLIP_X | FLIP_Y;// 7

	public static final int HCENTER = 1;
	public static final int VCENTER = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int TOP = 16;
	public static final int BOTTOM = 32;
	public static final int BASELINE = 64;

	public static final int LEFT_TOP = LEFT | TOP;
	public static final int RIGHT_TOP = RIGHT | TOP;
	public static final int LEFT_BOTTOM = LEFT | BOTTOM;
	public static final int RIGHT_BOTTOM = RIGHT | BOTTOM;
	public static final int LEFT_CENTER = LEFT | HCENTER;
	public static final int RIGHT_CENTER = RIGHT | HCENTER;
	public static final int TOP_CENTER = TOP | VCENTER;
	public static final int BOTTOM_CENTER = BOTTOM | VCENTER;
	public static final int CENTER = HCENTER | VCENTER;
}