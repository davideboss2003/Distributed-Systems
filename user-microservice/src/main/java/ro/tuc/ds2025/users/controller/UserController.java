package ro.tuc.ds2025.users.controller;

import ro.tuc.ds2025.users.dto.CreateUserDTO;
import ro.tuc.ds2025.users.dto.UpdateUserDTO;
import ro.tuc.ds2025.users.dto.UserDTO;
import ro.tuc.ds2025.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;  //COMENTAT PENTRU TESTARE
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/*
@RestController =@Contoller + @ResponseBody
spune spring clasa asta nu ret pag HTML ci corp JSON direc in rasp html

@RequiredArgsConstructor
Lombok generează constructor cu toate câmpurile final. Aici e doar UserService userService – deci nu mai scrii tu constructor
*/

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService; //nu are @Autowired Lombok ii injecteaza constructorul generat de @RequiredArgsConstructor

    //ret 200 ok + corp json array de UserDTO
    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")   //doar adminu poate vedea lista completa - COMENTAT PENTRU TESTARE
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //adminul poate vedea orice user
    //userul insusi poate sa-si vada propriul recod(compara usenamul ret de service cu numele din token/session)
    //#id este SpEL – Spring Expression Language – și injectează valoarea din path
    @GetMapping("/{id}")//detalii despte user dupa UUID
    //@PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).username == authentication.name")  //COMENTAT PENTRU TESTARE
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    //creez un user nou admin only ret 201 created + noul userDTO in body
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")  //COMENTAT PENTRU TESTARE
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        UserDTO createdUser = userService.createUser(createUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    //asta e un update total
    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")  //COMENTAT PENTRU TESTARE
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return ResponseEntity.ok(userService.updateUser(id, updateUserDTO));
    }


    //sterge si ret  204 no content dara body iar doar adminul are
    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")  //COMENTAT PENTRU TESTARE
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    //e f util cand vrei sa verifici rapid daca exiata usename
    @GetMapping("/username/{username}")
    //@PreAuthorize("hasRole('ADMIN')")  //COMENTAT PENTRU TESTARE
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

}
