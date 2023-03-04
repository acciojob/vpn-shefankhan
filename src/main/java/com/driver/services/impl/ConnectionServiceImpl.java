package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        User user = userRepository2.findById(userId).get();

        if(user.getConnected()){
            throw new Exception("Already connected");
        } else if (countryName.equalsIgnoreCase(user.getOriginalCountry().getCountryName().toString())){
            return user;
        }else {
            if(user.getServiceProviderList().size()==0 || valOfServiceProviderId(user, countryName)!=Integer.MAX_VALUE){
                throw new Exception("Unable to connect");
            } else {
                //maskedIp is "updatedCountryCode.serviceProviderId.userId"
                int serviceProviderId = valOfServiceProviderId(user, countryName);
                String countryCode = getCountryCode(countryName,serviceProviderId);
                String maskedIp = countryCode+"."+serviceProviderId+"."+userId;

                user.setMaskedIp(maskedIp);
                user.setConnected(true);
                Connection connection = new Connection();
                connection.setUser(user);
                connection.setServiceProvider(serviceProviderRepository2.findById(serviceProviderId).get());
                user.getConnectionList().add(connection);

                userRepository2.save(user);
                return userRepository2.findById(userId).get();
            }
        }
    }
    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if(!user.getConnected()){
            throw new Exception("Already disconnected");
        }
        user.setMaskedIp(null);
        user.setConnected(false);
        return userRepository2.save(user);
    }

    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        String senderCountry = sender.getOriginalCountry().getCountryName().toString();
        String receiverCountry ;
        if(receiver.getConnected()){
            receiverCountry = getCountry(receiver);
        } else {
            receiverCountry = receiver.getOriginalCountry().getCountryName().toString();
        }
        if(!senderCountry.equalsIgnoreCase(receiverCountry) && !sender.getConnected()){
            try{
                sender = connect(senderId, receiverCountry);

            } catch (Exception e){
                throw new Exception("Cannot establish communication");
            }
        }
        senderCountry = getCountry(sender);
        if(!senderCountry.equals(receiverCountry) && sender.getConnected()){
            throw new Exception("they are not in the same country");
        }
        return sender;
    }

    private int valOfServiceProviderId(User user, String countryName){
        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
        int serviceProviderId = Integer.MAX_VALUE;

        for(ServiceProvider serviceProvider: serviceProviderList){
            List<Country> countryList = serviceProvider.getCountryList();
            for(Country country : countryList){
                if(countryName.equalsIgnoreCase(country.getCountryName().toString())){
                    serviceProviderId = Math.min(serviceProviderId, serviceProvider.getId());
                }
            }

        }
        return serviceProviderId;
    }
    private String getCountryCode(String countryName, int id){
        List<Country> countryList = (serviceProviderRepository2.findById(id).get()).getCountryList();
        for(Country country : countryList){
            if(countryName.equalsIgnoreCase(country.getCountryName().toString())){
                return country.getCode();
            }
        }
        return "";
    }
    private String getCountry(User user){
        if(!user.getConnected()){
            return user.getOriginalCountry().getCountryName().toString();
        }
        String maskedIp = user.getMaskedIp();
        String countryCode = maskedIp.substring(0,3);
        String country;
        if(countryCode.equals("001")){
            country = "IND";
        } else if(countryCode.equals("002")){
            country = "USA";
        } else if(countryCode.equals("003")){
            country = "AUS";
        } else if(countryCode.equals("004")){
            country = "CHI";
        }else {
            country = "JPN";
        }
        return country;
    }
}
