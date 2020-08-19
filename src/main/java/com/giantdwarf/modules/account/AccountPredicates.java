package com.giantdwarf.modules.account;

import com.giantdwarf.modules.tag.Tag;
import com.giantdwarf.modules.zone.Zone;
import com.querydsl.core.types.Predicate;

import java.util.Set;

public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {

        com.giantdwarf.modules.account.QAccount account = com.giantdwarf.modules.account.QAccount.account;
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }
}
