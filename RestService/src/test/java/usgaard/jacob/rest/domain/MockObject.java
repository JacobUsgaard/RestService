package usgaard.jacob.rest.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class MockObject implements Serializable {

	private static final long serialVersionUID = 8523753891895804179L;

	@Id
	@Column
	private String name;

	@Column
	private Integer age;

	@Column
	private float pi;

	public MockObject() {
		super();
	}

	public MockObject(String name, Integer age, float pi) {
		super();
		this.name = name;
		this.age = age;
		this.pi = pi;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public float getPi() {
		return pi;
	}

	public void setPi(float pi) {
		this.pi = pi;
	}

	@Override
	public String toString() {
		return "MockObject [name=" + name + ", age=" + age + ", pi=" + pi + "]";
	}

}
