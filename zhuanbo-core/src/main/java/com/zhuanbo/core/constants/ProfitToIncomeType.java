package com.zhuanbo.core.constants;

public enum ProfitToIncomeType {
	
	PROFITTYPE_101(101, 3),
	PROFITTYPE_102(102, 4),
	PROFITTYPE_201(201, 5),
	PROFITTYPE_202(202, 6),
	PROFITTYPE_301(301, 7),
	PROFITTYPE_302(302, 8)
	;

	private int profitType;
	
	private int incomeType;
	

	public int getProfitType() {
		return profitType;
	}

	public void setProfitType(int profitType) {
		this.profitType = profitType;
	}



	public int getIncomeType() {
		return incomeType;
	}



	public void setIncomeType(int incomeType) {
		this.incomeType = incomeType;
	}


	ProfitToIncomeType(int profitType, int incomeType) {
        this.profitType = profitType;
        this.incomeType = incomeType;
    }
	

	public static ProfitToIncomeType parasByProfitType (int id) {
		for (ProfitToIncomeType type : ProfitToIncomeType.values()) {
			if (type.getProfitType() == id) {
				return type;
			}
		}
		return null;
	}
	
	public static ProfitToIncomeType parasByIncomeType (int id) {
		for (ProfitToIncomeType type : ProfitToIncomeType.values()) {
			if (type.getIncomeType() == id) {
				return type;
			}
		}
		return null;
	}
}
