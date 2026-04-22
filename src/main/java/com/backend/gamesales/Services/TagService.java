package com.backend.gamesales.Services;

import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Model.Tags;
import com.backend.gamesales.Repository.TagRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@NoArgsConstructor
public class TagService {

    @Autowired
    private  TagRepository tagRepository;
    public Set<Tags> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return new HashSet<>();
        Set<Tags> tags = new HashSet<>(tagRepository.findAllById(tagIds));
        if (tags.size() != tagIds.size()) {
            throw new RuntimeException("Uno o más tags no existen");
        }
        return tags;
    }

    public List<Tags> getAll() {
        return tagRepository.findAll();
    }
    public Tags getById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + id));
    }


}
