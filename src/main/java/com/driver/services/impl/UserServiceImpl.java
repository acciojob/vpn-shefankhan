package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        //The originalIp of the user should be "countryCode.userId"
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        Country country = new Country();
        CountryName countryName1;

        if(countryName.equalsIgnoreCase("IND")){
            countryName1 = CountryName.IND;
        } else if(countryName.equalsIgnoreCase("AUS")){
            countryName1 = CountryName.AUS;
        }else if(countryName.equalsIgnoreCase("USA")){
            countryName1 = CountryName.USA;
        }else if(countryName.equalsIgnoreCase("CHI")){
            countryName1 = CountryName.CHI;
        } else {
            countryName1 = CountryName.JPN;
        }

        country.setCountryName(countryName1);
        String countryCode = countryName1.toCode();

        String originalIp = countryCode+"."+user.getId();
        country.setCode(countryCode);
        country.setUser(user);
        user.setOriginalCountry(country);

        return userRepository3.save(user);
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user = userRepository3.findById(userId).get();
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();
        serviceProvider.getUsers().add(user);
        user.getServiceProviderList().add(serviceProvider);

        return userRepository3.save(user);
    }
}
