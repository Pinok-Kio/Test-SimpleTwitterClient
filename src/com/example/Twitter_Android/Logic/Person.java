package com.example.Twitter_Android.Logic;

public class Person implements Comparable<Person> {
	private final String name;
	private final String screenName;
	private final String profileImage;
	private final String location;
	private final String description;
	private boolean isFriend;
	private final long ID;

	//------------------------------------------------------------------------------------------------------------------
	public Person(String name, String screenName, String profileImage, String location, String description, boolean isFriend, long ID) {
		this.name = name;
		this.screenName = screenName;
		this.profileImage = profileImage;
		this.location = location;
		this.description = description;
		this.isFriend = isFriend;
		this.ID = ID;
	}
	//------------------------------------------------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	//------------------------------------------------------------------------------------------------------------------

	public String getScreenName() {
		return screenName;
	}
	//------------------------------------------------------------------------------------------------------------------

	public String getProfileImage() {
		return profileImage;
	}
	//------------------------------------------------------------------------------------------------------------------

	public String getLocation() {
		return location;
	}
	//------------------------------------------------------------------------------------------------------------------

	public String getDescription() {
		return description;
	}

	//------------------------------------------------------------------------------------------------------------------

	public long getID() {
		return ID;
	}

	//------------------------------------------------------------------------------------------------------------------
	public boolean isFriend() {
		return isFriend;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "Name: " + name + "\nFrom: " + location + "\nDescription: " + description;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Person.class != o.getClass()) return false;

		Person person = (Person) o;

		return ID == person.ID
				&& description.equals(person.description)
				&& location.equals(person.location)
				&& name.equals(person.name)
				&& profileImage.equals(person.profileImage)
				&& screenName.equals(person.screenName);
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return name.hashCode()
				+ profileImage.hashCode()
				+ location.hashCode()
				+ description.hashCode()
				+ screenName.hashCode()
				+ Long.valueOf(ID).hashCode();
	}

	@Override
	public int compareTo(Person another) {
		return name.compareToIgnoreCase(another.getName());
	}

	public void changeFriendshipRelations() {
		isFriend = !isFriend;
	}
}
