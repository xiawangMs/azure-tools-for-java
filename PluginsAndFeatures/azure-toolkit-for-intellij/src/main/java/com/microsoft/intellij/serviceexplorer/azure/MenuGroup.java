package com.microsoft.intellij.serviceexplorer.azure;

public enum MenuGroup {
    GROUP_1(100), GROUP_2(101), GROUP_3(103);
    private final int groupNumber;

    MenuGroup(final int defaultGroupNumber) {
        this.groupNumber = defaultGroupNumber;
    }

    public int getGroupNumber() {
        return groupNumber;
    }
}
