package com.zhuanbo.core.constants;

public enum PartnerEnum {

	OPERATION("operation"),
	/**司令合伙人2高级合伙人：团队含10名司令合伙人*/
	PARTNER2HIGH_PARTNER_TEAM("partner2high_partner_team"),
	/**司令合伙人2高级合伙人：团队含直推3个合伙人*/
	PARTNER2HIGH_PARTNER_ZS("partner2high_partner_zs"),
	/**高级合伙人2总监：团队含2直推高级合伙人*/
	HIGH_PARTNER2DIRECTOR_ZS("high_partner2director_zs"),
	/**总监2高级总监：团队含2直推总监*/
	DIRECTOR2HIGH_DIRECTOR_ZS("director2high_director_zs"),
	/**高级总监2代言人：团队5个高级总监*/
	HIGH_DIRECTOR2SPOKESMAN_TEAM("high_director2spokesman_team"),
	/**高级总监2代言人：直推3个高级总监*/
	HIGH_DIRECTOR2SPOKESMAN_ZS("high_director2spokesman_zs"),
	/**代言人2高级代言人：直推3个代言人*/
	SPOKESMAN2HIGH_SPOKESMAN_ZS("spokesman2high_spokesman_zs");

	private String value;

	PartnerEnum(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}
}
