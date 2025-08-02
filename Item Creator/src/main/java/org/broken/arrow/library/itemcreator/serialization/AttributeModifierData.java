package org.broken.arrow.library.itemcreator.serialization;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

import java.util.UUID;

public class AttributeModifierData {
    public String attribute;
    public String name;
    public double amount;
    public String operation;
    public String uuid;

    public static AttributeModifierData from(Attribute attr, AttributeModifier mod) {
        AttributeModifierData data = new AttributeModifierData();
        data.attribute = attr.name();
        data.name = mod.getName();
        data.amount = mod.getAmount();
        data.operation = mod.getOperation().name();
        data.uuid = mod.getUniqueId().toString();
        return data;
    }

    public AttributeModifier toModifier() {
        return new AttributeModifier(UUID.fromString(uuid), name, amount, AttributeModifier.Operation.valueOf(operation));
    }
}