package com.zzhalex.justdirethings.common.portal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortalLinkDataTest {

    @Test
    void portalFavoritesRoundTripThroughNbt() {
        PortalLinkData data = new PortalLinkData();
        data.setFavoriteIndex(2);
        assertEquals(2, PortalLinkData.read(PortalLinkData.write(data)).getFavoriteIndex());
    }
}
