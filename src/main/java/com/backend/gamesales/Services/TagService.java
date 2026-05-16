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

    public Tags createTag(String name) {
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("The tag already exists");
        }
        Tags tag = new Tags();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new NotFoundException("Tag not found: " + id);
        }
        tagRepository.deleteById(id);
    }

    @Autowired
    private TagRepository tagRepository;

    public Set<Tags> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty())
            return new HashSet<>();
        Set<Tags> tags = new HashSet<>(tagRepository.findAllById(tagIds));
        if (tags.size() != tagIds.size()) {
            throw new RuntimeException("One or more tags do not exist");
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
