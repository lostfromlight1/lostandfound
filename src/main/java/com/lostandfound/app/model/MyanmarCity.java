package com.lostandfound.app.model;

import lombok.Getter;

@Getter
public enum MyanmarCity {
    // Major Cities and Capitals
    YANGON("Yangon", "Yangon Region"),
    MANDALAY("Mandalay", "Mandalay Region"),
    NAYPYIDAW("Naypyidaw", "Naypyidaw Union Territory"),
    TAUNGGYI("Taunggyi", "Shan State"),
    MAWLAMYINE("Mawlamyine", "Mon State"),
    BAGO("Bago", "Bago Region"),
    PATHEIN("Pathein", "Ayeyarwady Region"),
    MYITKYINA("Myitkyina", "Kachin State"),
    MONYWA("Monywa", "Sagaing Region"),
    SITTWE("Sittwe", "Rakhine State"),
    PYAY("Pyay", "Bago Region"),
    PAKOKKU("Pakokku", "Magway Region"),
    MAGWAY("Mag way", "Mag way Region"),
    HPA_AN("Hpa-An", "Kain State"),
    LASHIO("Lashio", "Shan State"),
    DAWEI("Dawei", "Tanintharyi Region"),
    MEIKTILA("Meiktila", "Mandalay Region"),
    PYIN_OO_LWIN("Pyin Oo Lwin", "Mandalay Region"),
    LOIKAW("Loikaw", "Kayah State"),
    HAKHA("Hakha", "Chin State"),
    MYEIK("Myeik", "Tanintharyi Region");

    private final String displayName;
    private final String region;

    MyanmarCity(String displayName, String region) {
        this.displayName = displayName;
        this.region = region;
    }

}