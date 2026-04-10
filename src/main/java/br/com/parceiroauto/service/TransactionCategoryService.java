package br.com.parceiroauto.service;

import br.com.parceiroauto.entity.Company;
import br.com.parceiroauto.entity.TransactionCategory;
import br.com.parceiroauto.entity.TransactionType;
import br.com.parceiroauto.repository.TransactionCategoryRepository;

import java.util.List;

public class TransactionCategoryService {

    private final TransactionCategoryRepository transactionCategoryRepository;

    public TransactionCategoryService(TransactionCategoryRepository transactionCategoryRepository) {
        this.transactionCategoryRepository = transactionCategoryRepository;
    }

    public TransactionCategory createCategory(Company company, String name, TransactionType tipo) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome da categoria nao pode ser vazio");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("Tipo da categoria nao pode ser nulo");
        }

        TransactionCategory existingCategory = transactionCategoryRepository.findByCompanyAndName(company, name);
        if (existingCategory != null) {
            throw new IllegalArgumentException("Ja existe uma categoria com esse nome para essa empresa");
        }

        TransactionCategory transactionCategory = new TransactionCategory(name.trim(), company, tipo, true);
        transactionCategoryRepository.save(transactionCategory);
        return transactionCategory;
    }

    public List<TransactionCategory> findByCompany(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        return transactionCategoryRepository.findByCompany(company);
    }

    public List<TransactionCategory> findActiveByCompanyAndType(Company company, TransactionType tipo) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("Tipo nao pode ser nulo");
        }

        return transactionCategoryRepository.findActiveByCompanyAndType(company, tipo);
    }

    public TransactionCategory updateCategory(
            Company company,
            TransactionCategory category,
            String name,
            TransactionType tipo,
            boolean active
    ) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (category == null) {
            throw new IllegalArgumentException("Categoria nao pode ser nula");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome da categoria nao pode ser vazio");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("Tipo da categoria nao pode ser nulo");
        }

        TransactionCategory existingCategory = transactionCategoryRepository.findByCompanyAndName(company, name);
        if (existingCategory != null && !existingCategory.getId().equals(category.getId())) {
            throw new IllegalArgumentException("Ja existe uma categoria com esse nome para essa empresa");
        }

        category.setName(name.trim());
        category.setTipo(tipo);
        category.setActive(active);
        transactionCategoryRepository.update(category);
        return category;
    }

    public void deleteCategory(Company company, TransactionCategory category) {
        if (company == null) {
            throw new IllegalArgumentException("Empresa nao pode ser nula");
        }

        if (category == null) {
            throw new IllegalArgumentException("Categoria nao pode ser nula");
        }

        if (!category.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("A categoria nao pertence a essa empresa");
        }

        transactionCategoryRepository.delete(category);
    }
}
