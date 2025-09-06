package dev.eduzavarce.pets.shared.core.domain;

public abstract class DoubleValueObject {
  private Double value;

  public DoubleValueObject(Double value) {
    this.value = value;
  }

  public Double value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DoubleValueObject that = (DoubleValueObject) o;
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
