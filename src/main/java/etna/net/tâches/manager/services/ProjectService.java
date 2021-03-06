package etna.net.tâches.manager.services;

import etna.net.tâches.manager.exceptions.ProjectIdException;
import etna.net.tâches.manager.exceptions.ProjectNotFoundException;
import etna.net.tâches.manager.repositories.BacklogRepository;
import etna.net.tâches.manager.repositories.ProjectRepository;
import etna.net.tâches.manager.repositories.UserRepository;
import etna.net.tâches.manager.domain.Backlog;
import etna.net.tâches.manager.domain.Project;
import etna.net.tâches.manager.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private UserRepository userRepository;

    public Project saveOrUpdateProject(Project project, String username){

        if(project.getId() != null){
            Project existingProject = projectRepository.findByProjectIdentifier(project.getProjectIdentifier());
            if(existingProject !=null &&(!existingProject.getProjectLeader().equals(username))){
                throw new ProjectNotFoundException("Projet non trouvé sur votre compte");
            }else if(existingProject == null){
                throw new ProjectNotFoundException("Projet ID: '"+project.getProjectIdentifier()+"' ne peut pas être update car il n'existe pas");
            }
        }

        try{

            User user = userRepository.findByUsername(username);
            project.setUser(user);
            project.setProjectLeader(user.getUsername());
            project.setProjectIdentifier(project.getProjectIdentifier().toUpperCase());

            if(project.getId()==null){
                Backlog backlog = new Backlog();
                project.setBacklog(backlog);
                backlog.setProject(project);
                backlog.setProjectIdentifier(project.getProjectIdentifier().toUpperCase());
            }

            if(project.getId()!=null){
                project.setBacklog(backlogRepository.findByProjectIdentifier(project.getProjectIdentifier().toUpperCase()));
            }

            return projectRepository.save(project);

        }catch (Exception e){
            throw new ProjectIdException("Projet ID '"+project.getProjectIdentifier().toUpperCase()+"' existe déjà");
        }

    }


    public Project findProjectByIdentifier(String projectId, String username){

        //Only want to return the project if the user looking for it is the owner

        Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());

        if(project == null){
            throw new ProjectIdException("Projet ID '"+projectId+"' n'existe pas");

        }

        if(!project.getProjectLeader().equals(username)){
            throw new ProjectNotFoundException("Projet  non trouvé sur votre compte");
        }



        return project;
    }

    public Iterable<Project> findAllProjects(String username){
        return projectRepository.findAllByProjectLeader(username);
    }


    public void deleteProjectByIdentifier(String projectid, String username){


        projectRepository.delete(findProjectByIdentifier(projectid, username));
    }

}
