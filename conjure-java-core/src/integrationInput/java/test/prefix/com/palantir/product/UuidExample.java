package test.prefix.com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.processing.Generated;

@JsonDeserialize(builder = UuidExample.Builder.class)
@Generated("com.palantir.conjure.java.types.BeanGenerator")
public final class UuidExample {
    private final UUID uuid;

    private UuidExample(UUID uuid) {
        validateFields(uuid);
        this.uuid = uuid;
    }

    @JsonProperty("uuid")
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof UuidExample && equalTo((UuidExample) other));
    }

    private boolean equalTo(UuidExample other) {
        return this.uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public String toString() {
        return "UuidExample{uuid: " + uuid + '}';
    }

    public static UuidExample of(UUID uuid) {
        return builder().uuid(uuid).build();
    }

    private static void validateFields(UUID uuid) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, uuid, "uuid");
        if (missingFields != null) {
            throw new SafeIllegalArgumentException(
                    "Some required fields have not been set", SafeArg.of("missingFields", missingFields));
        }
    }

    private static List<String> addFieldIfMissing(List<String> prev, Object fieldValue, String fieldName) {
        List<String> missingFields = prev;
        if (fieldValue == null) {
            if (missingFields == null) {
                missingFields = new ArrayList<>(1);
            }
            missingFields.add(fieldName);
        }
        return missingFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Generated("com.palantir.conjure.java.types.BeanBuilderGenerator")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        boolean _buildInvoked;

        private UUID uuid;

        private Builder() {}

        public Builder from(UuidExample other) {
            checkNotBuilt();
            uuid(other.getUuid());
            return this;
        }

        @JsonSetter("uuid")
        public Builder uuid(@Nonnull UUID uuid) {
            checkNotBuilt();
            this.uuid = Preconditions.checkNotNull(uuid, "uuid cannot be null");
            return this;
        }

        public UuidExample build() {
            checkNotBuilt();
            this._buildInvoked = true;
            return new UuidExample(uuid);
        }

        private void checkNotBuilt() {
            Preconditions.checkState(!_buildInvoked, "Build has already been called");
        }
    }
}
