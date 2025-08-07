package org.broken.arrow.library.itemcreator.serialization;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.UUID;

public class AttributeModifierWrapper {
    private String uuid;
    private String equipmentSlot;
    private String attribute;
    private String name;
    private double amount;
    private String operation;

    public static AttributeModifierWrapper from(Attribute attr, AttributeModifier mod) {
        AttributeModifierWrapper data = new AttributeModifierWrapper();
        data.attribute = attr.name();
        data.name = mod.getName();
        data.amount = mod.getAmount();
        data.operation = mod.getOperation().name();
        data.uuid = mod.getUniqueId().toString();
        data.equipmentSlot = mod.getSlot() != null ? mod.getSlot().name() : null;
        return data;
    }

    public AttributeEntry toModifier() {
        return new AttributeEntry(new AttributeModifier(UUID.fromString(uuid), name, amount, AttributeModifier.Operation.valueOf(operation), getEquipmentSlot()));
    }

    private EquipmentSlot getEquipmentSlot() {
        final String slot = this.equipmentSlot;
        return slot != null ? EquipmentSlot.valueOf(slot) : null;
    }


    public class AttributeEntry {
        private final String attribute;
        private final AttributeModifier attributeModifier;

        public AttributeEntry(@Nonnull final AttributeModifier attributeModifier) {
            this.attribute = AttributeModifierWrapper.this.attribute;
            this.attributeModifier = attributeModifier;
        }

        public Attribute getAttribute() {
            return Attribute.valueOf(attribute);
        }

        public AttributeModifier getAttributeModifier() {
            return attributeModifier;
        }
    }
}