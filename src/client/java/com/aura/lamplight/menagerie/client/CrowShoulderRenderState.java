package com.aura.lamplight.menagerie.client;

public interface CrowShoulderRenderState {
    boolean menagerie$hasCrowOnLeftShoulder();

    boolean menagerie$hasCrowOnRightShoulder();

    void menagerie$setCrowOnLeftShoulder(boolean present);

    void menagerie$setCrowOnRightShoulder(boolean present);
}
