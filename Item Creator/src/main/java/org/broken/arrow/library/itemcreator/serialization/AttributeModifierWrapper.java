package org.broken.arrow.library.itemcreator.serialization;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * A serializable wrapper for Minecraft's {@link AttributeModifier},
 * allowing attribute modifiers to be stored and reconstructed (e.g., from JSON or other data formats).
 * <p>
 * This class stores all relevant data (attribute type, name, amount, operation, slot, UUID)
 * as simple string/primitive fields so it can be easily serialized.
 * It also provides conversion methods to and from Bukkit's attribute classes.
 */
public class AttributeModifierWrapper {
    private String uuid;
    private String equipmentSlot;
    private String attribute;
    private String name;
    private String operation;
    private double amount;

    /**
     * Creates a wrapper from the given {@link Attribute} and {@link AttributeModifier}.
     *
     * @param attr the attribute type
     * @param mod  the attribute modifier to wrap
     * @return a new {@code AttributeModifierWrapper} containing the modifier's data
     */
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

    /**
     * Converts this wrapper back into an {@link AttributeEntry}, containing both
     * the attribute type and the constructed {@link AttributeModifier}.
     *
     * @return an {@code AttributeEntry} representing this wrapper's data
     * @throws IllegalArgumentException if the stored attribute or operation names are invalid
     */
    public AttributeEntry toModifier() {
        return new AttributeEntry(new AttributeModifier(UUID.fromString(uuid), name, amount, AttributeModifier.Operation.valueOf(operation), getEquipmentSlot()));
    }

    /**
     * Resolves the {@link EquipmentSlot} from the stored slot name, or {@code null} if none was set.
     *
     * @return the corresponding equipment slot, or {@code null}
     */
    private EquipmentSlot getEquipmentSlot() {
        final String slot = this.equipmentSlot;
        return slot != null ? EquipmentSlot.valueOf(slot) : null;
    }

    /**
     * Represents an immutable pairing of an {@link Attribute} and its {@link AttributeModifier}.
     * <p>
     * This is returned by {@link AttributeModifierWrapper#toModifier()} to provide both
     * the attribute type and modifier together for application in Bukkit's API.
     */
    public class AttributeEntry {
        private final String attribute;
        private final AttributeModifier attributeModifier;

        /**
         * Creates an {@code AttributeEntry} with the specified attribute modifier.
         * The attribute type is inherited from the enclosing {@link AttributeModifierWrapper}.
         *
         * @param attributeModifier the attribute modifier to associate
         */
        public AttributeEntry(@Nonnull final AttributeModifier attributeModifier) {
            this.attribute = AttributeModifierWrapper.this.attribute;
            this.attributeModifier = attributeModifier;
        }

        /**
         * Gets the Bukkit {@link Attribute} represented by this entry.
         *
         * @return the attribute type
         * @throws IllegalArgumentException if the stored attribute name is invalid
         */
        public Attribute getAttribute() {
            return Attribute.valueOf(attribute);
        }

        /**
         * Gets the attribute modifier represented by this entry.
         *
         * @return the attribute modifier
         */
        public AttributeModifier getAttributeModifier() {
            return attributeModifier;
        }
    }
}