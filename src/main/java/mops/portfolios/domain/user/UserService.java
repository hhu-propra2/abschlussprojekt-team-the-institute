package mops.portfolios.domain.user;

import java.util.ArrayList;
import java.util.List;
import mops.portfolios.domain.group.Group;
import mops.portfolios.domain.group.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserService {

  @Autowired
  transient UserRepository repository;

  @Autowired
  transient GroupRepository groupRepository;

  /**
   * Gets list fo groups from one username.
   *
   * @return list fo groups
   */
  public List<Group> getGroupsByUserName(String userName) {
    User user = repository.findOneByName(userName);
    if (null == user) {
      return new ArrayList<>();
    }
    return user.getGroups();
  }

  public boolean isUserNameInGroup(String userName, Group group) {
    List<Group> groups = repository.findById(userName).get().getGroups();
    return groups.contains(group);
  }
}
