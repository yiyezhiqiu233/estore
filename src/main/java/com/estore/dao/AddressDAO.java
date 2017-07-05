package com.estore.dao;

import com.estore.object.Address;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface AddressDAO {
	int insertUserAddress(Address address);

	List<Address> queryAllAddressesOfUser(int userId);

	int updateUserAddress(int addressId, String address);

	int deleteAddress(int addressId);
}
