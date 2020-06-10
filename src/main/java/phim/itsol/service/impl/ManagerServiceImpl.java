package phim.itsol.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phim.itsol.domain.Manager;
import phim.itsol.domain.Role;
import phim.itsol.dto.ManagerDto;
import phim.itsol.exception.EmailExistException;
import phim.itsol.exception.UsernameExistException;
import phim.itsol.repo.ManagerRepository;
import phim.itsol.security.AuthoritiesConstants;
import phim.itsol.security.SecurityUtils;
import phim.itsol.service.ManagerService;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ManagerServiceImpl implements ManagerService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public void register(ManagerDto managerDto) throws UsernameExistException, EmailExistException {
        log.trace("Service to register user in web site");
        String username = managerDto.getManagerUsername();
        Optional<Manager> checkUsername = managerRepository.findOneWithRoleListByManagerUsername(username);
        if(checkUsername.isPresent()){
            throw new UsernameExistException("Username has exist in database");
        }
//        Optional<Manager> checkEmail = managerRepository.findOneWithAuthoritiesByEmail(username);
//        if(checkEmail.isPresent()){
//            throw new EmailExistException("Email has exist in database");
//        }
        Manager entity = modelMapper.map(managerDto, Manager.class);
        entity.setManagerPassword(passwordEncoder.encode(entity.getManagerPassword()));
        Set<Role> roleList = new HashSet<>(Collections
                .singletonList(new Role(AuthoritiesConstants.MANAGER)));
        entity.setRoleList(roleList);
//        entity.setActivated(Boolean.FALSE);
        managerRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Manager> getManagerWithRoleList() {
        return SecurityUtils.getCurrentUserLogin().flatMap(managerRepository::findOneWithRoleListByManagerUsername);
    }
}
