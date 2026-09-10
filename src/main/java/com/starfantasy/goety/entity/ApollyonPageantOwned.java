package com.starfantasy.goety.entity;

import java.util.UUID;

/** Marker shared by transient entities created for one Apollyon pageant. */
public interface ApollyonPageantOwned {
    UUID pageantOwnerUuid();
}
